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
package com.djrapitops.plan.db.sql.tables;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.patches.SessionAFKTimePatch;
import com.djrapitops.plan.db.patches.SessionsOptimizationPatch;
import com.djrapitops.plan.db.patches.Version10Patch;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plan.db.sql.parsing.Sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Table that represents plan_sessions.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link SessionAFKTimePatch}
 * {@link SessionsOptimizationPatch}
 *
 * @author Rsl1122
 */
public class SessionsTable extends Table {

    public static final String TABLE_NAME = "plan_sessions";

    public static final String ID = "id";
    public static final String USER_UUID = "uuid";
    public static final String SERVER_UUID = "server_uuid";
    public static final String SESSION_START = "session_start";
    public static final String SESSION_END = "session_end";
    public static final String MOB_KILLS = "mob_kills";
    public static final String DEATHS = "deaths";
    public static final String AFK_TIME = "afk_time";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
            + USER_UUID + ", "
            + SESSION_START + ", "
            + SESSION_END + ", "
            + DEATHS + ", "
            + MOB_KILLS + ", "
            + AFK_TIME + ", "
            + SERVER_UUID
            + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static final String SELECT_SESSION_ID_STATEMENT = "(SELECT " + TABLE_NAME + "." + ID + " FROM " + TABLE_NAME +
            " WHERE " + TABLE_NAME + "." + USER_UUID + "=?" +
            " AND " + TABLE_NAME + "." + SERVER_UUID + "=?" +
            " AND " + SESSION_START + "=?" +
            " AND " + SESSION_END + "=? LIMIT 1)";

    public SessionsTable(SQLDB db) {
        super(TABLE_NAME, db);
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .column(SESSION_START, Sql.LONG).notNull()
                .column(SESSION_END, Sql.LONG).notNull()
                .column(MOB_KILLS, Sql.INT).notNull()
                .column(DEATHS, Sql.INT).notNull()
                .column(AFK_TIME, Sql.LONG).notNull()
                .toString();
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
                .where(USER_UUID + "=?")
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
                    int id = set.getInt(ID);
                    long start = set.getLong(SESSION_START);
                    long end = set.getLong(SESSION_END);
                    UUID serverUUID = UUID.fromString(set.getString(SERVER_UUID));

                    long timeAFK = set.getLong(AFK_TIME);

                    int deaths = set.getInt(DEATHS);
                    int mobKills = set.getInt(MOB_KILLS);
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
                " (SUM(" + SESSION_END + ") - SUM(" + SESSION_START + ")) as playtime" +
                " FROM " + tableName +
                " WHERE " + SESSION_START + ">?" +
                " AND " + USER_UUID + "=?" +
                " AND " + SERVER_UUID + "=?";

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
                " (SUM(" + SESSION_END + ") - SUM(" + SESSION_START + ")) as playtime" +
                " FROM " + tableName +
                " WHERE " + SESSION_START + ">?" +
                " AND " + SERVER_UUID + "=?";

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
                " WHERE (" + SESSION_START + " >= ?)" +
                " AND " + USER_UUID + "=?" +
                " AND " + SERVER_UUID + "=?";

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
                tableName + "." + ID + ", " +
                USER_UUID + ", " +
                SESSION_START + ", " +
                SESSION_END + ", " +
                DEATHS + ", " +
                MOB_KILLS + ", " +
                AFK_TIME +
                " FROM " + tableName +
                " WHERE " + SERVER_UUID + "=?";

        return query(new QueryStatement<Map<UUID, List<Session>>>(sql, 5000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Map<UUID, List<Session>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<Session>> sessionsByUser = new HashMap<>();
                while (set.next()) {
                    int id = set.getInt(ID);
                    UUID uuid = UUID.fromString(set.getString(USER_UUID));
                    long start = set.getLong(SESSION_START);
                    long end = set.getLong(SESSION_END);

                    int deaths = set.getInt(DEATHS);
                    int mobKills = set.getInt(MOB_KILLS);

                    long timeAFK = set.getLong(AFK_TIME);

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
                " MAX(" + SESSION_END + ") as last_seen, " +
                USER_UUID +
                " FROM " + tableName +
                " GROUP BY " + USER_UUID;

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

    public Map<Integer, Integer> getIDServerIDRelation() {
        String sql = "SELECT " +
                ID + ", " +
                "(SELECT plan_servers.id FROM plan_servers WHERE plan_servers.uuid=" + SERVER_UUID + ") as server_id" +
                " FROM " + tableName;

        return query(new QueryAllStatement<Map<Integer, Integer>>(sql, 10000) {
            @Override
            public Map<Integer, Integer> processResults(ResultSet set) throws SQLException {
                Map<Integer, Integer> idServerIdMap = new HashMap<>();
                while (set.next()) {
                    idServerIdMap.put(set.getInt(ID), set.getInt("server_id"));
                }
                return idServerIdMap;
            }
        });
    }
}