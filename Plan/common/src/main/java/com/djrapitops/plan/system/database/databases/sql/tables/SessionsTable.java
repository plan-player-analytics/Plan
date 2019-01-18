/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Table that represents plan_sessions.
 *
 * Patches related to this table:
 * {@link com.djrapitops.plan.system.database.databases.sql.patches.Version10Patch}
 * {@link com.djrapitops.plan.system.database.databases.sql.patches.SessionAFKTimePatch}
 * {@link com.djrapitops.plan.system.database.databases.sql.patches.SessionsOptimizationPatch}
 *
 * @author Rsl1122
 */
public class SessionsTable extends UserUUIDTable {

    public static final String TABLE_NAME = "plan_sessions";

    private String insertStatement;

    public SessionsTable(SQLDB db) {
        super(TABLE_NAME, db);
        insertStatement = "INSERT INTO " + tableName + " ("
                + Col.UUID + ", "
                + Col.SESSION_START + ", "
                + Col.SESSION_END + ", "
                + Col.DEATHS + ", "
                + Col.MOB_KILLS + ", "
                + Col.AFK_TIME + ", "
                + Col.SERVER_UUID
                + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(this.tableName)
                .primaryKeyIDColumn(supportsMySQLQueries, Col.ID)
                .column(Col.UUID, Sql.varchar(36)).notNull()
                .column(Col.SERVER_UUID, Sql.varchar(36)).notNull()
                .column(Col.SESSION_START, Sql.LONG).notNull()
                .column(Col.SESSION_END, Sql.LONG).notNull()
                .column(Col.MOB_KILLS, Sql.INT).notNull()
                .column(Col.DEATHS, Sql.INT).notNull()
                .column(Col.AFK_TIME, Sql.LONG).notNull()
                .primaryKey(supportsMySQLQueries, Col.ID)
                .toString()
        );
    }

    /**
     * Used to get the sessionID of a newly inserted row.
     *
     * @param uuid    UUID of the player
     * @param session session inserted.
     * @return ID of the inserted session or -1 if session has not been inserted.
     */
    private int getSessionID(UUID uuid, Session session) {
        String sql = "SELECT " + Col.ID + " FROM " + tableName +
                " WHERE " + Col.UUID + "=?" +
                " AND " + Col.SESSION_START + "=?" +
                " AND " + Col.SESSION_END + "=?";

        return query(new QueryStatement<Integer>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setLong(2, session.getUnsafe(SessionKeys.START));
                statement.setLong(3, session.getValue(SessionKeys.END).orElse(System.currentTimeMillis()));
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getInt(Col.ID.get());
                }
                return -1;
            }
        });
    }

    /**
     * Used to save a session, with all it's information into the database.
     * <p>
     * Also saves WorldTimes and Kills.
     *
     * @param uuid    UUID of the player.
     * @param session Session of the player that has ended ({@code endSession} has been called)
     */
    public void saveSession(UUID uuid, Session session) {
        saveSessionInformation(uuid, session);
        int sessionID = getSessionID(uuid, session);
        if (sessionID == -1) {
            throw new IllegalStateException("Session was not Saved!");
        }

        db.getWorldTimesTable().saveWorldTimes(uuid, sessionID, session.getUnsafe(SessionKeys.WORLD_TIMES));
        db.getKillsTable().savePlayerKills(uuid, sessionID, session.getPlayerKills());
    }

    /**
     * Saves Session's Information to the Session Table.
     * <p>
     * Does not save Kills or WorldTimes.
     *
     * @param uuid    UUID of the player.
     * @param session Session of the player that has ended ({@code endSession} has been called)
     */
    private void saveSessionInformation(UUID uuid, Session session) {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setLong(2, session.getUnsafe(SessionKeys.START));
                statement.setLong(3, session.getUnsafe(SessionKeys.END));
                statement.setInt(4, session.getUnsafe(SessionKeys.DEATH_COUNT));
                statement.setInt(5, session.getUnsafe(SessionKeys.MOB_KILL_COUNT));
                statement.setLong(6, session.getUnsafe(SessionKeys.AFK_TIME));
                statement.setString(7, getServerUUID().toString());
            }
        });
    }

    /**
     * Returns a Map containing Lists of sessions, key as ServerName.
     * <p>
     * Does not include Kills or WorldTimes.
     * Use {@code getSessions} to get full Sessions.
     *
     * @param uuid UUID of the player
     * @return Map with Sessions that don't contain Kills or WorldTimes.
     */
    private Map<UUID, List<Session>> getSessionInformation(UUID uuid) {
        String sql = Select.from(tableName, "*")
                .where(Col.UUID + "=?")
                .toString();

        return query(new QueryStatement<Map<UUID, List<Session>>>(sql, 10000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Map<UUID, List<Session>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<Session>> sessionsByServer = new HashMap<>();
                while (set.next()) {
                    int id = set.getInt(Col.ID.get());
                    long start = set.getLong(Col.SESSION_START.get());
                    long end = set.getLong(Col.SESSION_END.get());
                    UUID serverUUID = UUID.fromString(set.getString(Col.SERVER_UUID.get()));

                    long timeAFK = set.getLong(Col.AFK_TIME.get());

                    int deaths = set.getInt(Col.DEATHS.get());
                    int mobKills = set.getInt(Col.MOB_KILLS.get());
                    List<Session> sessions = sessionsByServer.getOrDefault(serverUUID, new ArrayList<>());
                    sessions.add(new Session(id, uuid, serverUUID, start, end, mobKills, deaths, timeAFK));
                    sessionsByServer.put(serverUUID, sessions);
                }
                return sessionsByServer;
            }
        });
    }

    /**
     * Used to get Playtime after Epoch ms on a server.
     *
     * @param uuid       UUID of the player.
     * @param serverUUID UUID of the server. @see ServerTable
     * @param afterDate  Epoch ms (Playtime after this date is calculated)
     * @return Milliseconds played after given epoch ms on the server. 0 if player or server not found.
     */
    public long getPlaytime(UUID uuid, UUID serverUUID, long afterDate) {
        String sql = "SELECT" +
                " (SUM(" + Col.SESSION_END + ") - SUM(" + Col.SESSION_START + ")) as playtime" +
                " FROM " + tableName +
                " WHERE " + Col.SESSION_START + ">?" +
                " AND " + Col.UUID + "=?" +
                " AND " + Col.SERVER_UUID + "=?";

        return query(new QueryStatement<Long>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, afterDate);
                statement.setString(2, uuid.toString());
                statement.setString(3, serverUUID.toString());
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getLong("playtime");
                }
                return 0L;
            }
        });
    }

    public Map<UUID, List<Session>> getSessions(UUID uuid) {
        Map<UUID, List<Session>> sessions = getSessionInformation(uuid);
        Map<Integer, Session> allSessions = sessions.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(s -> s.getUnsafe(SessionKeys.DB_ID), Function.identity()));

        db.getKillsTable().addKillsToSessions(uuid, allSessions);
        db.getKillsTable().addDeathsToSessions(uuid, allSessions);
        db.getWorldTimesTable().addWorldTimesToSessions(uuid, allSessions);
        return sessions;
    }

    /**
     * Get Total Playtime of a Player on THIS server.
     *
     * @param uuid UUID of the player.
     * @return Milliseconds played on THIS server. 0 if player or server not found.
     */
    public long getPlaytime(UUID uuid) {
        return getPlaytime(uuid, getServerUUID());
    }

    /**
     * Get Playtime of a Player after Epoch ms on THIS server.
     *
     * @param uuid      UUID of the player.
     * @param afterDate Epoch ms (Playtime after this date is calculated)
     * @return Milliseconds played on THIS server. 0 if player or server not found.
     */
    public long getPlaytime(UUID uuid, long afterDate) {
        return getPlaytime(uuid, getServerUUID(), afterDate);
    }

    /**
     * Get Total Playtime of a Player on a server.
     *
     * @param uuid       UUID of the player.
     * @param serverUUID UUID of the server. @see ServerTable
     * @return Milliseconds played on the server. 0 if player or server not found.
     */
    public long getPlaytime(UUID uuid, UUID serverUUID) {
        return getPlaytime(uuid, serverUUID, 0L);
    }

    /**
     * Used to get Playtime after a date of a Server.
     *
     * @param serverUUID UUID of the server.
     * @param afterDate  Epoch ms (Playtime after this date is calculated)
     * @return Milliseconds played  after given epoch ms on the server. 0 if server not found.
     */
    public long getPlaytimeOfServer(UUID serverUUID, long afterDate) {
        String sql = "SELECT" +
                " (SUM(" + Col.SESSION_END + ") - SUM(" + Col.SESSION_START + ")) as playtime" +
                " FROM " + tableName +
                " WHERE " + Col.SESSION_START + ">?" +
                " AND " + Col.SERVER_UUID + "=?";

        return query(new QueryStatement<Long>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, afterDate);
                statement.setString(2, serverUUID.toString());
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getLong("playtime");
                }
                return 0L;
            }
        });
    }

    /**
     * Used to get the Total Playtime of a Server.
     *
     * @param serverUUID UUID of the server.
     * @return Milliseconds played on the server. 0 if server not found.
     */
    public long getPlaytimeOfServer(UUID serverUUID) {
        return getPlaytimeOfServer(serverUUID, 0L);
    }

    /**
     * Used to get total Session count of a Player on a server after a given epoch ms.
     *
     * @param uuid       UUID of the player.
     * @param serverUUID UUID of the server.
     * @param afterDate  Epoch ms (Session count after this date is calculated)
     * @return How many sessions player has. 0 if player or server not found.
     */
    public int getSessionCount(UUID uuid, UUID serverUUID, long afterDate) {
        String sql = "SELECT" +
                " COUNT(*) as logintimes" +
                " FROM " + tableName +
                " WHERE (" + Col.SESSION_START + " >= ?)" +
                " AND " + Col.UUID + "=?" +
                " AND " + Col.SERVER_UUID + "=?";

        return query(new QueryStatement<Integer>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, afterDate);
                statement.setString(2, uuid.toString());
                statement.setString(3, serverUUID.toString());
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getInt("logintimes");
                }
                return 0;
            }
        });
    }

    /**
     * Used to get total Session count of a Player on THIS server.
     *
     * @param uuid UUID of the player.
     * @return How many sessions player has. 0 if player or server not found.
     */
    public int getSessionCount(UUID uuid) {
        return getSessionCount(uuid, 0L);
    }

    /**
     * Used to get total Session count of a Player on THIS server after a given epoch ms.
     *
     * @param uuid      UUID of the player.
     * @param afterDate Epoch ms (Session count after this date is calculated)
     * @return How many sessions player has. 0 if player or server not found.
     */
    public int getSessionCount(UUID uuid, long afterDate) {
        return getSessionCount(uuid, getServerUUID(), afterDate);
    }

    public Map<UUID, List<Session>> getSessionInfoOfServer(UUID serverUUID) {
        String sql = "SELECT " +
                tableName + "." + Col.ID + ", " +
                Col.UUID + ", " +
                Col.SESSION_START + ", " +
                Col.SESSION_END + ", " +
                Col.DEATHS + ", " +
                Col.MOB_KILLS + ", " +
                Col.AFK_TIME +
                " FROM " + tableName +
                " WHERE " + Col.SERVER_UUID + "=?";

        return query(new QueryStatement<Map<UUID, List<Session>>>(sql, 5000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Map<UUID, List<Session>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<Session>> sessionsByUser = new HashMap<>();
                while (set.next()) {
                    int id = set.getInt(Col.ID.get());
                    UUID uuid = UUID.fromString(set.getString(Col.UUID.get()));
                    long start = set.getLong(Col.SESSION_START.get());
                    long end = set.getLong(Col.SESSION_END.get());

                    int deaths = set.getInt(Col.DEATHS.get());
                    int mobKills = set.getInt(Col.MOB_KILLS.get());

                    long timeAFK = set.getLong(Col.AFK_TIME.get());

                    List<Session> sessions = sessionsByUser.getOrDefault(uuid, new ArrayList<>());
                    sessions.add(new Session(id, uuid, serverUUID, start, end, mobKills, deaths, timeAFK));
                    sessionsByUser.put(uuid, sessions);
                }
                return sessionsByUser;
            }
        });
    }

    public Map<UUID, List<Session>> getSessionInfoOfServer() {
        return getSessionInfoOfServer(getServerUUID());
    }

    public Map<UUID, Long> getLastSeenForAllPlayers() {
        String sql = "SELECT" +
                " MAX(" + Col.SESSION_END + ") as last_seen, " +
                Col.UUID +
                " FROM " + tableName +
                " GROUP BY " + Col.UUID;

        return query(new QueryAllStatement<Map<UUID, Long>>(sql, 20000) {
            @Override
            public Map<UUID, Long> processResults(ResultSet set) throws SQLException {
                Map<UUID, Long> lastSeenMap = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString("uuid"));
                    long lastSeen = set.getLong("last_seen");
                    lastSeenMap.put(uuid, lastSeen);
                }
                return lastSeenMap;
            }
        });
    }

    public Map<UUID, Map<UUID, List<Session>>> getAllSessions(boolean getKillsAndWorldTimes) {
        String sql = "SELECT " +
                Col.ID + ", " +
                Col.UUID + ", " +
                Col.SERVER_UUID + ", " +
                Col.SESSION_START + ", " +
                Col.SESSION_END + ", " +
                Col.DEATHS + ", " +
                Col.MOB_KILLS + ", " +
                Col.AFK_TIME +
                " FROM " + tableName;

        return query(new QueryAllStatement<Map<UUID, Map<UUID, List<Session>>>>(sql, 20000) {
            @Override
            public Map<UUID, Map<UUID, List<Session>>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<UUID, List<Session>>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(Col.SERVER_UUID.get()));
                    UUID uuid = UUID.fromString(set.getString(Col.UUID.get()));

                    Map<UUID, List<Session>> sessionsByUser = map.getOrDefault(serverUUID, new HashMap<>());
                    List<Session> sessions = sessionsByUser.getOrDefault(uuid, new ArrayList<>());

                    long start = set.getLong(Col.SESSION_START.get());
                    long end = set.getLong(Col.SESSION_END.get());

                    int deaths = set.getInt(Col.DEATHS.get());
                    int mobKills = set.getInt(Col.MOB_KILLS.get());
                    int id = set.getInt(Col.ID.get());

                    long timeAFK = set.getLong(Col.AFK_TIME.get());

                    Session session = new Session(id, uuid, serverUUID, start, end, mobKills, deaths, timeAFK);
                    sessions.add(session);

                    sessionsByUser.put(uuid, sessions);
                    map.put(serverUUID, sessionsByUser);
                }
                if (getKillsAndWorldTimes) {
                    db.getKillsTable().addKillsToSessions(map);
                    db.getWorldTimesTable().addWorldTimesToSessions(map);
                }
                return map;
            }
        });
    }

    public void insertSessions(Map<UUID, Map<UUID, List<Session>>> allSessions, boolean saveKillsAndWorldTimes) {
        if (Verify.isEmpty(allSessions)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (UUID serverUUID : allSessions.keySet()) {
                    for (Map.Entry<UUID, List<Session>> entry : allSessions.get(serverUUID).entrySet()) {
                        UUID uuid = entry.getKey();
                        List<Session> sessions = entry.getValue();

                        for (Session session : sessions) {
                            statement.setString(1, uuid.toString());
                            statement.setLong(2, session.getUnsafe(SessionKeys.START));
                            statement.setLong(3, session.getUnsafe(SessionKeys.END));
                            statement.setInt(4, session.getUnsafe(SessionKeys.DEATH_COUNT));
                            statement.setInt(5, session.getUnsafe(SessionKeys.MOB_KILL_COUNT));
                            statement.setLong(6, session.getUnsafe(SessionKeys.AFK_TIME));
                            statement.setString(7, serverUUID.toString());
                            statement.addBatch();
                        }
                    }
                }
            }
        });
        if (saveKillsAndWorldTimes) {
            Map<UUID, Map<UUID, List<Session>>> savedSessions = getAllSessions(false);
            matchSessionIDs(allSessions, savedSessions);
            db.getKillsTable().savePlayerKills(allSessions);
            db.getWorldTimesTable().saveWorldTimes(allSessions);
        }
    }

    /**
     * Sessions should be saved before calling this method.
     *
     * @param allSessions      Sessions to match IDs to (contain extra data)
     * @param allSavedSessions Sessions in the Database.
     */
    private void matchSessionIDs(Map<UUID, Map<UUID, List<Session>>> allSessions, Map<UUID, Map<UUID, List<Session>>> allSavedSessions) {
        for (UUID serverUUID : allSessions.keySet()) {
            Map<UUID, List<Session>> serverSessions = allSessions.get(serverUUID);
            Map<UUID, List<Session>> savedServerSessions = allSavedSessions.get(serverUUID);

            for (Map.Entry<UUID, List<Session>> entry : serverSessions.entrySet()) {
                UUID uuid = entry.getKey();
                List<Session> sessions = entry.getValue();

                List<Session> savedSessions = savedServerSessions.get(uuid);
                if (savedSessions == null) {
                    throw new IllegalStateException("Some of the sessions being matched were not saved.");
                }

                matchSessions(sessions, savedSessions);
            }
        }
    }

    /**
     * Used by matchSessionIDs method.
     * <p>
     * Matches IDs of Sessions with by sessionStart.
     * Assumes that both lists are from the same user and server.
     *
     * @param sessions      Sessions of Player in a Server.
     * @param savedSessions Sessions of Player in a Server in the db.
     */
    private void matchSessions(List<Session> sessions, List<Session> savedSessions) {
        Map<Long, List<Session>> sessionsByStart = turnToMapByStart(sessions);
        Map<Long, List<Session>> savedSessionsByStart = turnToMapByStart(savedSessions);

        for (Map.Entry<Long, List<Session>> sessionEntry : sessionsByStart.entrySet()) {
            long start = sessionEntry.getKey();
            if (!savedSessionsByStart.containsKey(start)) {
                throw new IllegalStateException("Some of the sessions being matched were not saved.");
            }
            Session savedSession = savedSessionsByStart.get(start).get(0);
            sessionEntry.getValue().forEach(
                    session -> session.setSessionID(savedSession.getUnsafe(SessionKeys.DB_ID))
            );
        }
    }

    private Map<Long, List<Session>> turnToMapByStart(List<Session> sessions) {
        Map<Long, List<Session>> sessionsByStart = new TreeMap<>();
        for (Session session : sessions) {
            long start = session.getUnsafe(SessionKeys.START);
            List<Session> sorted = sessionsByStart.getOrDefault(start, new ArrayList<>());
            sorted.add(session);
            sessionsByStart.put(start, sorted);
        }
        return sessionsByStart;
    }

    public Map<Integer, Integer> getIDServerIDRelation() {
        String sql = "SELECT " +
                Col.ID + ", " +
                "(SELECT plan_servers.id FROM plan_servers WHERE plan_servers.uuid=" + Col.SERVER_UUID + ") as server_id" +
                " FROM " + tableName;

        return query(new QueryAllStatement<Map<Integer, Integer>>(sql, 10000) {
            @Override
            public Map<Integer, Integer> processResults(ResultSet set) throws SQLException {
                Map<Integer, Integer> idServerIdMap = new HashMap<>();
                while (set.next()) {
                    idServerIdMap.put(set.getInt(Col.ID.get()), set.getInt("server_id"));
                }
                return idServerIdMap;
            }
        });
    }

    public enum Col implements Column {
        UUID(UserUUIDTable.Col.UUID.get()),
        ID("id"),
        SERVER_UUID("server_uuid"),
        SESSION_START("session_start"),
        SESSION_END("session_end"),
        MOB_KILLS("mob_kills"),
        DEATHS("deaths"),
        AFK_TIME("afk_time");

        private final String column;

        Col(String column) {
            this.column = column;
        }

        @Override
        public String get() {
            return toString();
        }

        @Override
        public String toString() {
            return column;
        }
    }
}