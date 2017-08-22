package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Select;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class SessionsTable extends UserIDTable {

    private final String columnSessionID = "id";
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
    public boolean createTable() {
        try {
            String serverTableName = serverTable.getTableName();
            String serverTableID = serverTable.getColumnID();
            String sql = TableSqlParser.createTable(this.tableName)
                    .primaryKeyIDColumn(usingMySQL, columnSessionID, Sql.LONG)
                    .column(columnUserID, Sql.INT).notNull()
                    .column(columnServerID, Sql.INT).notNull()
                    .column(columnSessionStart, Sql.LONG).notNull()
                    .column(columnSessionEnd, Sql.LONG).notNull()
                    .column(columnMobKills, Sql.INT).notNull()
                    .column(columnDeaths, Sql.INT).notNull()
                    .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                    .foreignKey(columnServerID, serverTableName, serverTableID)
                    .primaryKey(usingMySQL, columnSessionID)
                    .toString();
            execute(sql);
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    /**
     * Removes User's Sessions from the Database.
     * <p>
     * // TODO KILLS SHOULD BE REMOVED FIRST.
     *
     * @param userId
     * @return
     */
    public boolean removeUserSessions(int userId) {
        return super.removeDataOf(userId);
    }

    public void saveSessionInformation(UUID uuid, Session session) throws SQLException {
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
                    + columnUserID + "=" + usersTable.statementSelectID + ", "
                    + "?, ?, ?, ?, "
                    + serverTable.statementSelectServerID + ")");
            statement.setString(1, uuid.toString());

            statement.setLong(2, session.getSessionStart());
            statement.setLong(3, session.getSessionEnd());
            statement.setInt(4, session.getDeaths());
            statement.setInt(5, session.getMobKills());

            statement.setString(6, Plan.getServerUUID().toString());
            statement.execute();
        } finally {
            endTransaction(statement);
            close(statement);
        }

        db.getWorldTimesTable().saveWorldTimes(session.getWorldTimes());
        db.getKillsTable().savePlayerKills(uuid, session.getPlayerKills());
    }

    public Map<String, List<Session>> getSessions(UUID uuid) throws SQLException {
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
                long id = set.getLong(columnSessionID);
                long start = set.getLong(columnSessionStart);
                long end = set.getLong(columnSessionEnd);
                String serverName = serverNames.get(set.getInt(columnServerID));

                int deaths = set.getInt(columnDeaths);
                int mobKills = set.getInt(columnMobKills);
                List<Session> sessions = sessionsByServer.getOrDefault(serverName, new ArrayList<>());
                sessions.add(new Session(id, start, end, deaths, mobKills));
            }
            return sessionsByServer;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public long getPlaytime(UUID uuid) throws SQLException {
        return getPlaytime(uuid, Plan.getServerUUID());
    }

    public long getPlaytime(UUID uuid, UUID serverUUID) throws SQLException {
        return getPlaytime(uuid, serverUUID, 0L);
    }

    public long getPlaytime(UUID uuid, UUID serverUUID, long afterDate) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT FROM " + tableName + " "
                    + "(SUM(" + columnSessionEnd + ") - SUM(" + columnSessionStart + ")) as playtime "
                    + "WHERE " + columnSessionStart + ">? AND "
                    + columnUserID + "=" + usersTable.statementSelectID + " AND "
                    + columnServerID + "=" + serverTable.statementSelectServerID);
            statement.setLong(1, afterDate);
            statement.setString(2, uuid.toString());
            statement.setString(3, serverUUID.toString());
            set = statement.executeQuery();
            if (set.next()) {
                return set.getLong("playtime");
            }
            return 0;
        } finally {
            close(set, statement);
        }
    }

    public Map<String, Long> getPlaytimeByServer(UUID uuid) throws SQLException {
        Map<Integer, String> serverNames = serverTable.getServerNames();
        Map<String, Long> playtimes = new HashMap<>();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT FROM " + tableName + " "
                    + "(SUM(" + columnSessionEnd + ") - SUM(" + columnSessionStart + ")) as playtime "
                    + "WHERE " + columnSessionStart + ">? AND "
                    + columnUserID + "=" + usersTable.statementSelectID); // TODO CONTINUE
            return playtimes;
        } finally {
            close(set, statement);
        }
    }
}
