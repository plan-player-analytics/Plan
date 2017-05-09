package main.java.com.djrapitops.plan.database.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.database.databases.SQLDB;

/**
 *
 * @author Rsl1122
 */
public class SessionsTable extends Table {

    private final String columnUserID;
    private final String columnSessionStart;
    private final String columnSessionEnd;

    public SessionsTable(SQLDB db, boolean usingMySQL) {
        super("plan_sessions", db, usingMySQL);
        columnUserID = "user_id";
        columnSessionStart = "session_start";
        columnSessionEnd = "session_end";
    }

    @Override
    public boolean createTable() {
        try {
            UsersTable usersTable = db.getUsersTable();
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnUserID + " integer NOT NULL, "
                    + columnSessionStart + " bigint NOT NULL, "
                    + columnSessionEnd + " bigint NOT NULL, "
                    + "FOREIGN KEY(" + columnUserID + ") REFERENCES " + usersTable.getTableName() + "(" + usersTable.getColumnID() + ")"
                    + ")"
            );
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    public List<SessionData> getSessionData(int userId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE (" + columnUserID + "=?)");
            statement.setInt(1, userId);
            set = statement.executeQuery();
            List<SessionData> sessions = new ArrayList<>();
            while (set.next()) {
                sessions.add(new SessionData(set.getLong(columnSessionStart), set.getLong(columnSessionEnd)));
            }
            set.close();
            statement.close();
            return sessions;
        } finally {
            close(set);
            close(statement);
        }
    }

    public boolean removeUserSessions(int userId) {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE (" + columnUserID + "=?)");
            statement.setInt(1, userId);
            statement.execute();
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        } finally {
            close(statement);
        }
    }

    public void saveSessionData(int userId, List<SessionData> sessions) throws SQLException {
        if (sessions == null) {
            return;
        }
        sessions.removeAll(getSessionData(userId));
        if (sessions.isEmpty()) {
            return;
        }
        PreparedStatement statement = null;
        try {

            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnUserID + ", "
                    + columnSessionStart + ", "
                    + columnSessionEnd
                    + ") VALUES (?, ?, ?)");

            boolean commitRequired = false;
            for (SessionData session : sessions) {
                long end = session.getSessionEnd();
                long start = session.getSessionStart();
                if (end < start) {
                    continue;
                }
                statement.setInt(1, userId);
                statement.setLong(2, start);
                statement.setLong(3, end);
                statement.addBatch();
                commitRequired = true;
            }
            if (commitRequired) {
                statement.executeBatch();
            }
        } finally {
            close(statement);
        }
    }
}
