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
            execute(TableSqlParser.createTable(tableName)
                    .primaryKeyIDColumn(usingMySQL, columnServerID, Sql.LONG)
                    .column(columnUserID, Sql.INT).notNull()
                    .column(columnServerID, Sql.INT).notNull()
                    .column(columnSessionStart, Sql.LONG).notNull()
                    .column(columnSessionEnd, Sql.LONG).notNull()
                    .column(columnMobKills, Sql.INT).notNull()
                    .column(columnDeaths, Sql.INT).notNull()
                    .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                    .foreignKey(columnServerID, serverTable.getTableName(), serverTable.getColumnID())
                    .primaryKey(usingMySQL, columnSessionID)
                    .toString()
            );
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
            close(set, statement);
        }
    }
}
