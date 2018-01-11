package com.djrapitops.plan.database.tables;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.DBCreateTableException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.database.databases.SQLDB;
import com.djrapitops.plan.database.processing.ExecStatement;
import com.djrapitops.plan.database.processing.QueryAllStatement;
import com.djrapitops.plan.database.processing.QueryStatement;
import com.djrapitops.plan.database.sql.Select;
import com.djrapitops.plan.database.sql.Sql;
import com.djrapitops.plan.database.sql.TableSqlParser;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 */
public class SessionsTable extends UserIDTable {

    private final String columnID = "id";
    private final String columnSessionStart = "session_start";
    private final String columnSessionEnd = "session_end";
    private final String columnServerID = "server_id";
    private final String columnMobKills = "mob_kills";
    private final String columnDeaths = "deaths";

    private final ServerTable serverTable;
    private String insertStatement;

    public SessionsTable(SQLDB db, boolean usingMySQL) {
        super("plan_sessions", db, usingMySQL);
        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " ("
                + columnUserID + ", "
                + columnSessionStart + ", "
                + columnSessionEnd + ", "
                + columnDeaths + ", "
                + columnMobKills + ", "
                + columnServerID
                + ") VALUES ("
                + usersTable.statementSelectID + ", "
                + "?, ?, ?, ?, "
                + serverTable.statementSelectServerID + ")";
    }

    @Override
    public void createTable() throws DBCreateTableException {
        createTable(TableSqlParser.createTable(this.tableName)
                .primaryKeyIDColumn(usingMySQL, columnID)
                .column(columnUserID, Sql.INT).notNull()
                .column(columnServerID, Sql.INT).notNull()
                .column(columnSessionStart, Sql.LONG).notNull()
                .column(columnSessionEnd, Sql.LONG).notNull()
                .column(columnMobKills, Sql.INT).notNull()
                .column(columnDeaths, Sql.INT).notNull()
                .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                .foreignKey(columnServerID, serverTable.getTableName(), serverTable.getColumnID())
                .primaryKey(usingMySQL, columnID)
                .toString()
        );
    }

    /**
     * Used to save a session, with all it's information into the database.
     * <p>
     * Also saves WorldTimes and Kills.
     *
     * @param uuid    UUID of the player.
     * @param session Session of the player that has ended ({@code endSession} has been called)
     * @throws SQLException DB Error
     */
    public void saveSession(UUID uuid, Session session) throws SQLException {
        saveSessionInformation(uuid, session);
        int sessionID = getSessionID(uuid, session);
        if (sessionID == -1) {
            throw new IllegalStateException("Session was not Saved!");
        }

        db.getWorldTimesTable().saveWorldTimes(uuid, sessionID, session.getWorldTimes());
        db.getKillsTable().savePlayerKills(uuid, sessionID, session.getPlayerKills());
    }

    /**
     * Saves Session's Information to the Session Table.
     * <p>
     * Does not save Kills or WorldTimes.
     *
     * @param uuid    UUID of the player.
     * @param session Session of the player that has ended ({@code endSession} has been called)
     * @throws SQLException DB Error
     */
    private void saveSessionInformation(UUID uuid, Session session) throws SQLException {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setLong(2, session.getSessionStart());
                statement.setLong(3, session.getSessionEnd());
                statement.setInt(4, session.getDeaths());
                statement.setInt(5, session.getMobKills());
                statement.setString(6, Plan.getServerUUID().toString());
            }
        });
    }

    /**
     * Used to get the sessionID of a newly inserted row.
     *
     * @param uuid    UUID of the player
     * @param session session inserted.
     * @return ID of the inserted session or -1 if session has not been inserted.
     */
    private int getSessionID(UUID uuid, Session session) throws SQLException {
        String sql = "SELECT " + columnID + " FROM " + tableName +
                " WHERE " + columnUserID + "=" + usersTable.statementSelectID +
                " AND " + columnSessionStart + "=?" +
                " AND " + columnSessionEnd + "=?";

        return query(new QueryStatement<Integer>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setLong(2, session.getSessionStart());
                statement.setLong(3, session.getSessionEnd());
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getInt(columnID);
                }
                return -1;
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
     * @throws SQLException DB Error
     */
    private Map<UUID, List<Session>> getSessionInformation(UUID uuid) throws SQLException {
        Map<Integer, UUID> serverUUIDs = serverTable.getServerUuids();
        String sql = Select.from(tableName, "*")
                .where(columnUserID + "=" + usersTable.statementSelectID)
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
                    int id = set.getInt(columnID);
                    long start = set.getLong(columnSessionStart);
                    long end = set.getLong(columnSessionEnd);
                    UUID serverUUID = serverUUIDs.get(set.getInt(columnServerID));

                    if (serverUUID == null) {
                        throw new IllegalStateException("Server not present");
                    }

                    int deaths = set.getInt(columnDeaths);
                    int mobKills = set.getInt(columnMobKills);
                    List<Session> sessions = sessionsByServer.getOrDefault(serverUUID, new ArrayList<>());
                    sessions.add(new Session(id, start, end, mobKills, deaths));
                    sessionsByServer.put(serverUUID, sessions);
                }
                return sessionsByServer;
            }
        });
    }

    public Map<UUID, List<Session>> getSessions(UUID uuid) throws SQLException {
        Map<UUID, List<Session>> sessions = getSessionInformation(uuid);
        Map<Integer, Session> allSessions = sessions.values().stream().flatMap(Collection::stream).collect(Collectors.toMap(Session::getSessionID, Function.identity()));

        db.getKillsTable().addKillsToSessions(uuid, allSessions);
        db.getWorldTimesTable().addWorldTimesToSessions(uuid, allSessions);
        return sessions;
    }

    /**
     * Get Total Playtime of a Player on THIS server.
     *
     * @param uuid UUID of the player.
     * @return Milliseconds played on THIS server. 0 if player or server not found.
     * @throws SQLException DB Error
     */
    public long getPlaytime(UUID uuid) throws SQLException {
        return getPlaytime(uuid, Plan.getServerUUID());
    }

    /**
     * Get Playtime of a Player after Epoch ms on THIS server.
     *
     * @param uuid      UUID of the player.
     * @param afterDate Epoch ms (Playtime after this date is calculated)
     * @return Milliseconds played on THIS server. 0 if player or server not found.
     * @throws SQLException DB Error
     */
    public long getPlaytime(UUID uuid, long afterDate) throws SQLException {
        return getPlaytime(uuid, Plan.getServerUUID(), afterDate);
    }

    /**
     * Get Total Playtime of a Player on a server.
     *
     * @param uuid       UUID of the player.
     * @param serverUUID UUID of the server. @see ServerTable
     * @return Milliseconds played on the server. 0 if player or server not found.
     * @throws SQLException DB Error
     */
    public long getPlaytime(UUID uuid, UUID serverUUID) throws SQLException {
        return getPlaytime(uuid, serverUUID, 0L);
    }

    /**
     * Used to get Playtime after Epoch ms on a server.
     *
     * @param uuid       UUID of the player.
     * @param serverUUID UUID of the server. @see ServerTable
     * @param afterDate  Epoch ms (Playtime after this date is calculated)
     * @return Milliseconds played after given epoch ms on the server. 0 if player or server not found.
     * @throws SQLException DB Error
     */
    public long getPlaytime(UUID uuid, UUID serverUUID, long afterDate) throws SQLException {
        String sql = "SELECT" +
                " (SUM(" + columnSessionEnd + ") - SUM(" + columnSessionStart + ")) as playtime" +
                " FROM " + tableName +
                " WHERE " + columnSessionStart + ">?" +
                " AND " + columnUserID + "=" + usersTable.statementSelectID +
                " AND " + columnServerID + "=" + serverTable.statementSelectServerID;

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

    /**
     * Used to get Totals of Playtime in a Map, sorted by ServerNames.
     *
     * @param uuid UUID of the Player.
     * @return key - ServerName, value ms played
     * @throws SQLException DB Error
     */
    public Map<String, Long> getPlaytimeByServer(UUID uuid) throws SQLException {
        return getPlaytimeByServer(uuid, 0L);
    }

    /**
     * Used to get Playtimes after a date in a Map, sorted by ServerNames.
     *
     * @param uuid      UUID of the Player.
     * @param afterDate Epoch ms (Playtime after this date is calculated)
     * @return key - ServerName, value ms played
     * @throws SQLException DB Error
     */
    public Map<String, Long> getPlaytimeByServer(UUID uuid, long afterDate) throws SQLException {
        Map<Integer, String> serverNames = serverTable.getServerNamesByID();
        String sql = "SELECT " +
                "(SUM(" + columnSessionEnd + ") - SUM(" + columnSessionStart + ")) as playtime, " +
                columnServerID +
                " FROM " + tableName +
                " WHERE " + columnSessionStart + ">?" +
                " AND " + columnUserID + "=" + usersTable.statementSelectID +
                " GROUP BY " + columnServerID;
        return query(new QueryStatement<Map<String, Long>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, afterDate);
                statement.setString(2, uuid.toString());
            }

            @Override
            public Map<String, Long> processResults(ResultSet set) throws SQLException {
                Map<String, Long> playtimes = new HashMap<>();
                while (set.next()) {
                    String serverName = serverNames.get(set.getInt(columnServerID));
                    long playtime = set.getLong("playtime");
                    playtimes.put(serverName, playtime);
                }
                return playtimes;
            }
        });
    }

    /**
     * Used to get the Total Playtime of THIS Server.
     *
     * @return Milliseconds played on the server. 0 if server not found.
     * @throws SQLException DB Error
     */
    public long getPlaytimeOfServer() throws SQLException {
        return getPlaytimeOfServer(Plan.getServerUUID());
    }

    /**
     * Used to get the Total Playtime of a Server.
     *
     * @param serverUUID UUID of the server.
     * @return Milliseconds played on the server. 0 if server not found.
     * @throws SQLException DB Error
     */
    public long getPlaytimeOfServer(UUID serverUUID) throws SQLException {
        return getPlaytimeOfServer(serverUUID, 0L);
    }

    /**
     * Used to get Playtime after a date of THIS Server.
     *
     * @param afterDate Epoch ms (Playtime after this date is calculated)
     * @return Milliseconds played  after given epoch ms on the server. 0 if server not found.
     * @throws SQLException DB Error
     */
    public long getPlaytimeOfServer(long afterDate) throws SQLException {
        return getPlaytimeOfServer(Plan.getServerUUID(), afterDate);
    }

    /**
     * Used to get Playtime after a date of a Server.
     *
     * @param serverUUID UUID of the server.
     * @param afterDate  Epoch ms (Playtime after this date is calculated)
     * @return Milliseconds played  after given epoch ms on the server. 0 if server not found.
     * @throws SQLException DB Error
     */
    public long getPlaytimeOfServer(UUID serverUUID, long afterDate) throws SQLException {
        String sql = "SELECT" +
                " (SUM(" + columnSessionEnd + ") - SUM(" + columnSessionStart + ")) as playtime" +
                " FROM " + tableName +
                " WHERE " + columnSessionStart + ">?" +
                " AND " + columnServerID + "=" + serverTable.statementSelectServerID;

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
     * Used to get total Session count of a Player on THIS server.
     *
     * @param uuid UUID of the player.
     * @return How many sessions player has. 0 if player or server not found.
     * @throws SQLException DB Error
     */
    public int getSessionCount(UUID uuid) throws SQLException {
        return getSessionCount(uuid, 0L);
    }

    /**
     * Used to get total Session count of a Player on THIS server after a given epoch ms.
     *
     * @param uuid      UUID of the player.
     * @param afterDate Epoch ms (Session count after this date is calculated)
     * @return How many sessions player has. 0 if player or server not found.
     * @throws SQLException DB Error
     */
    public int getSessionCount(UUID uuid, long afterDate) throws SQLException {
        return getSessionCount(uuid, Plan.getServerUUID(), afterDate);
    }

    /**
     * Used to get total Session count of a Player on a server.
     *
     * @param uuid       UUID of the player.
     * @param serverUUID UUID of the server.
     * @return How many sessions player has. 0 if player or server not found.
     * @throws SQLException DB Error
     */
    public int getSessionCount(UUID uuid, UUID serverUUID) throws SQLException {
        return getSessionCount(uuid, serverUUID, 0L);
    }

    /**
     * Used to get total Session count of a Player on a server after a given epoch ms.
     *
     * @param uuid       UUID of the player.
     * @param serverUUID UUID of the server.
     * @param afterDate  Epoch ms (Session count after this date is calculated)
     * @return How many sessions player has. 0 if player or server not found.
     * @throws SQLException DB Error
     */
    public int getSessionCount(UUID uuid, UUID serverUUID, long afterDate) throws SQLException {
        String sql = "SELECT" +
                " COUNT(*) as logintimes" +
                " FROM " + tableName +
                " WHERE (" + columnSessionStart + " >= ?)" +
                " AND " + columnUserID + "=" + usersTable.statementSelectID +
                " AND " + columnServerID + "=" + serverTable.statementSelectServerID;

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

    public String getColumnID() {
        return columnID;
    }

    public Map<UUID, List<Session>> getSessionInfoOfServer() throws SQLException {
        return getSessionInfoOfServer(Plan.getServerUUID());
    }

    public Map<UUID, List<Session>> getSessionInfoOfServer(UUID serverUUID) throws SQLException {
        String usersIDColumn = usersTable + "." + usersTable.getColumnID();
        String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
        String sql = "SELECT " +
                tableName + "." + columnID + ", " +
                columnSessionStart + ", " +
                columnSessionEnd + ", " +
                columnDeaths + ", " +
                columnMobKills + ", " +
                usersUUIDColumn +
                " FROM " + tableName +
                " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                " WHERE " + columnServerID + "=" + serverTable.statementSelectServerID;

        return query(new QueryStatement<Map<UUID, List<Session>>>(sql, 5000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Map<UUID, List<Session>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<Session>> sessionsByUser = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString("uuid"));
                    long start = set.getLong(columnSessionStart);
                    long end = set.getLong(columnSessionEnd);

                    int deaths = set.getInt(columnDeaths);
                    int mobKills = set.getInt(columnMobKills);
                    List<Session> sessions = sessionsByUser.getOrDefault(uuid, new ArrayList<>());
                    sessions.add(new Session(set.getInt(columnID), start, end, mobKills, deaths));
                    sessionsByUser.put(uuid, sessions);
                }
                return sessionsByUser;
            }
        });
    }

    // TODO Write tests for this method
    public long getLastSeen(UUID uuid) throws SQLException {
        String sql = "SELECT" +
                " MAX(" + columnSessionEnd + ") as last_seen" +
                " FROM " + tableName +
                " WHERE " + columnUserID + "=" + usersTable.statementSelectID;

        return query(new QueryStatement<Long>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getLong("last_seen");
                }
                return 0L;
            }
        });
    }

    // TODO Write tests for this method
    public Map<UUID, Long> getLastSeenForAllPlayers() throws SQLException {
        String usersIDColumn = usersTable + "." + usersTable.getColumnID();
        String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
        String sql = "SELECT" +
                " MAX(" + columnSessionEnd + ") as last_seen, " +
                usersUUIDColumn +
                " FROM " + tableName +
                " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                " GROUP BY uuid";

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

    public Map<UUID, Map<UUID, List<Session>>> getAllSessions(boolean getKillsAndWorldTimes) throws SQLException {
        String usersIDColumn = usersTable + "." + usersTable.getColumnID();
        String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
        String serverIDColumn = serverTable + "." + serverTable.getColumnID();
        String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
        String sql = "SELECT " +
                tableName + "." + columnID + ", " +
                columnSessionStart + ", " +
                columnSessionEnd + ", " +
                columnDeaths + ", " +
                columnMobKills + ", " +
                usersUUIDColumn + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                " JOIN " + serverTable + " on " + serverIDColumn + "=" + columnServerID;

        return query(new QueryAllStatement<Map<UUID, Map<UUID, List<Session>>>>(sql, 20000) {
            @Override
            public Map<UUID, Map<UUID, List<Session>>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<UUID, List<Session>>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    Map<UUID, List<Session>> sessionsByUser = map.getOrDefault(serverUUID, new HashMap<>());
                    List<Session> sessions = sessionsByUser.getOrDefault(uuid, new ArrayList<>());

                    long start = set.getLong(columnSessionStart);
                    long end = set.getLong(columnSessionEnd);

                    int deaths = set.getInt(columnDeaths);
                    int mobKills = set.getInt(columnMobKills);
                    int id = set.getInt(columnID);

                    Session session = new Session(id, start, end, mobKills, deaths);
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

    public void insertSessions(Map<UUID, Map<UUID, List<Session>>> allSessions, boolean saveKillsAndWorldTimes) throws SQLException {
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
                            statement.setLong(2, session.getSessionStart());
                            statement.setLong(3, session.getSessionEnd());
                            statement.setInt(4, session.getDeaths());
                            statement.setInt(5, session.getMobKills());
                            statement.setString(6, serverUUID.toString());
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
        Map<Long, Session> sessionsByStart = sessions.stream().collect(Collectors.toMap(Session::getSessionStart, Function.identity()));
        Map<Long, Session> savedSessionsByStart = savedSessions.stream().collect(Collectors.toMap(Session::getSessionStart, Function.identity()));
        for (Map.Entry<Long, Session> sessionEntry : sessionsByStart.entrySet()) {
            long start = sessionEntry.getKey();
            Session savedSession = savedSessionsByStart.get(start);
            if (savedSession == null) {
                throw new IllegalStateException("Some of the sessions being matched were not saved.");
            }
            Session session = sessionEntry.getValue();
            session.setSessionID(savedSession.getSessionID());
        }
    }

    String getcolumnServerID() {
        return columnServerID;
    }
}