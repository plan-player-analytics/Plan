package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Select;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

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

    /**
     * @param db
     * @param usingMySQL
     */
    public SessionsTable(SQLDB db, boolean usingMySQL) {
        super("plan_sessions", db, usingMySQL);
        serverTable = db.getServerTable();
    }

    /**
     * @return
     */
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
     * @throws SQLException
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
     * @throws SQLException
     */
    private void saveSessionInformation(UUID uuid, Session session) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnUserID + ", "
                    + columnSessionStart + ", "
                    + columnSessionEnd + ", "
                    + columnDeaths + ", "
                    + columnMobKills + ", "
                    + columnServerID
                    + ") VALUES ("
                    + usersTable.statementSelectID + ", "
                    + "?, ?, ?, ?, "
                    + serverTable.statementSelectServerID + ")");
            statement.setString(1, uuid.toString());

            statement.setLong(2, session.getSessionStart());
            statement.setLong(3, session.getSessionEnd());
            statement.setInt(4, session.getDeaths());
            statement.setInt(5, session.getMobKills());
            statement.setString(6, Plan.getServerUUID().toString());

            statement.execute();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    /**
     * Used to get the sessionID of a newly inserted row.
     *
     * @param uuid    UUID of the player
     * @param session session inserted.
     * @return ID of the inserted session or -1 if session has not been inserted.
     */
    private int getSessionID(UUID uuid, Session session) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT " + columnID + " FROM " + tableName +
                    " WHERE " + columnUserID + "=" + usersTable.statementSelectID +
                    " AND " + columnSessionStart + "=?" +
                    " AND " + columnSessionEnd + "=?");
            statement.setString(1, uuid.toString());
            statement.setLong(2, session.getSessionStart());
            statement.setLong(3, session.getSessionEnd());
            set = statement.executeQuery();
            if (set.next()) {
                return set.getInt(columnID);
            }
            return -1;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    /**
     * Returns a Map containing Lists of sessions, key as ServerName.
     * <p>
     * Does not include Kills or WorldTimes.
     * Use {@code getSessions} to get full Sessions.
     *
     * @param uuid UUID of the player
     * @return Map with Sessions that don't contain Kills or WorldTimes.
     * @throws SQLException
     */
    private Map<String, List<Session>> getSessionInformation(UUID uuid) throws SQLException {
        Map<Integer, String> serverNames = serverTable.getServerNames();
        Map<String, List<Session>> sessionsByServer = new HashMap<>();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, "*")
                    .where(columnUserID + "=" + usersTable.statementSelectID)
                    .toString());
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            while (set.next()) {
                int id = set.getInt(columnID);
                long start = set.getLong(columnSessionStart);
                long end = set.getLong(columnSessionEnd);
                String serverName = serverNames.get(set.getInt(columnServerID));

                if (serverName == null) {
                    throw new IllegalStateException("Server not present");
                }

                int deaths = set.getInt(columnDeaths);
                int mobKills = set.getInt(columnMobKills);
                List<Session> sessions = sessionsByServer.getOrDefault(serverName, new ArrayList<>());
                sessions.add(new Session(id, start, end, deaths, mobKills));
                sessionsByServer.put(serverName, sessions);
            }
            return sessionsByServer;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public Map<String, List<Session>> getSessions(UUID uuid) throws SQLException {
        Map<String, List<Session>> sessions = getSessionInformation(uuid);
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
     * @throws SQLException
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
     * @throws SQLException
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
     * @throws SQLException
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
     * @throws SQLException
     */
    public long getPlaytime(UUID uuid, UUID serverUUID, long afterDate) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT" +
                    " (SUM(" + columnSessionEnd + ") - SUM(" + columnSessionStart + ")) as playtime" +
                    " FROM " + tableName +
                    " WHERE " + columnSessionStart + ">?" +
                    " AND " + columnUserID + "=" + usersTable.statementSelectID +
                    " AND " + columnServerID + "=" + serverTable.statementSelectServerID);
            statement.setLong(1, afterDate);
            statement.setString(2, uuid.toString());
            statement.setString(3, serverUUID.toString());
            set = statement.executeQuery();
            if (set.next()) {
                return set.getLong("playtime");
            }
            return 0;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    /**
     * Used to get Totals of Playtime in a Map, sorted by ServerNames.
     *
     * @param uuid UUID of the Player.
     * @return key - ServerName, value ms played
     * @throws SQLException
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
     * @throws SQLException
     */
    public Map<String, Long> getPlaytimeByServer(UUID uuid, long afterDate) throws SQLException {
        Map<Integer, String> serverNames = serverTable.getServerNames();
        Map<String, Long> playtimes = new HashMap<>();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT " +
                    "(SUM(" + columnSessionEnd + ") - SUM(" + columnSessionStart + ")) as playtime, " +
                    columnServerID +
                    " FROM " + tableName +
                    " WHERE " + columnSessionStart + ">?" +
                    " AND " + columnUserID + "=" + usersTable.statementSelectID);
            statement.setLong(1, afterDate);
            statement.setString(2, uuid.toString());
            set = statement.executeQuery();
            while (set.next()) {
                String serverName = serverNames.get(set.getInt(columnServerID));
                long playtime = set.getLong("playtime");
                playtimes.put(serverName, playtime);
            }
            return playtimes;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    /**
     * Used to get the Total Playtime of THIS Server.
     *
     * @return Milliseconds played on the server. 0 if server not found.
     * @throws SQLException
     */
    public long getPlaytimeOfServer() throws SQLException {
        return getPlaytimeOfServer(Plan.getServerUUID());
    }

    /**
     * Used to get the Total Playtime of a Server.
     *
     * @param serverUUID UUID of the server.
     * @return Milliseconds played on the server. 0 if server not found.
     * @throws SQLException
     */
    public long getPlaytimeOfServer(UUID serverUUID) throws SQLException {
        return getPlaytimeOfServer(serverUUID, 0L);
    }

    /**
     * Used to get Playtime after a date of THIS Server.
     *
     * @param afterDate Epoch ms (Playtime after this date is calculated)
     * @return Milliseconds played  after given epoch ms on the server. 0 if server not found.
     * @throws SQLException
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
     * @throws SQLException
     */
    public long getPlaytimeOfServer(UUID serverUUID, long afterDate) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT" +
                    " (SUM(" + columnSessionEnd + ") - SUM(" + columnSessionStart + ")) as playtime" +
                    " FROM " + tableName +
                    " WHERE " + columnSessionStart + ">?" +
                    " AND " + columnServerID + "=" + serverTable.statementSelectServerID);
            statement.setLong(1, afterDate);
            statement.setString(2, serverUUID.toString());
            set = statement.executeQuery();
            if (set.next()) {
                return set.getLong("playtime");
            }
            return 0;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    /**
     * Used to get total Session count of a Player on THIS server.
     *
     * @param uuid UUID of the player.
     * @return How many sessions player has. 0 if player or server not found.
     * @throws SQLException
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
     * @throws SQLException
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
     * @throws SQLException
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
     * @throws SQLException
     */
    public int getSessionCount(UUID uuid, UUID serverUUID, long afterDate) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT" +
                    " COUNT(*) as logintimes" +
                    " FROM " + tableName +
                    " WHERE (" + columnSessionStart + " >= ?)" +
                    " AND " + columnUserID + "=" + usersTable.statementSelectID +
                    " AND " + columnServerID + "=" + serverTable.statementSelectServerID);
            statement.setLong(1, afterDate);
            statement.setString(2, uuid.toString());
            statement.setString(3, serverUUID.toString());
            set = statement.executeQuery();
            if (set.next()) {
                return set.getInt("logintimes");
            }
            return 0;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public String getColumnID() {
        return columnID;
    }

    public Map<UUID, List<Session>> getSessionInfoOfServer() throws SQLException {
        return getSessionInfoOfServer(Plan.getServerUUID());
    }

    public Map<UUID, List<Session>> getSessionInfoOfServer(UUID serverUUID) throws SQLException {
        Optional<Integer> id = serverTable.getServerID(serverUUID);
        if (!id.isPresent()) {
            return new HashMap<>();
        }
        int serverID = id.get();
        Map<UUID, List<Session>> sessionsByUser = new HashMap<>();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            String usersIDColumn = usersTable + "." + usersTable.getColumnID();
            String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";

            statement = prepareStatement("SELECT " +
                    columnSessionStart + ", " +
                    columnSessionEnd + ", " +
                    columnDeaths + ", " +
                    columnMobKills + ", " +
                    usersUUIDColumn +
                    " FROM " + tableName +
                    " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                    " WHERE " + columnServerID + "=" + serverTable.statementSelectServerID
            );
            statement.setFetchSize(5000);
            statement.setString(1, serverUUID.toString());
            set = statement.executeQuery();
            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("uuid"));
                long start = set.getLong(columnSessionStart);
                long end = set.getLong(columnSessionEnd);

                int deaths = set.getInt(columnDeaths);
                int mobKills = set.getInt(columnMobKills);
                List<Session> sessions = sessionsByUser.getOrDefault(uuid, new ArrayList<>());
                sessions.add(new Session(serverID, start, end, deaths, mobKills));
                sessionsByUser.put(uuid, sessions);
            }
            return sessionsByUser;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }
}
