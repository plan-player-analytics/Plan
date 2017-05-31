package main.java.com.djrapitops.plan.database.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.Benchmark;

/**
 *
 * @author Rsl1122
 */
public class SessionsTable extends Table {

    private final String columnUserID;
    private final String columnSessionStart;
    private final String columnSessionEnd;

    /**
     *
     * @param db
     * @param usingMySQL
     */
    public SessionsTable(SQLDB db, boolean usingMySQL) {
        super("plan_sessions", db, usingMySQL);
        columnUserID = "user_id";
        columnSessionStart = "session_start";
        columnSessionEnd = "session_end";
    }

    /**
     *
     * @return
     */
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

    /**
     *
     * @param userId
     * @return
     * @throws SQLException
     */
    public List<SessionData> getSessionData(int userId) throws SQLException {
        Benchmark.start("Get Sessions");
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
            Benchmark.stop("Get Sessions");
        }
    }

    /**
     *
     * @param userId
     * @return
     */
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

    /**
     *
     * @param userId
     * @param sessions
     * @throws SQLException
     */
    public void saveSessionData(int userId, List<SessionData> sessions) throws SQLException {
        if (sessions == null) {
            return;
        }
        Benchmark.start("Save Sessions");
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
            Benchmark.stop("Save Sessions");
        }
    }

    /**
     *
     * @param ids
     * @return
     * @throws SQLException
     */
    public Map<Integer, List<SessionData>> getSessionData(Collection<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        Benchmark.start("Get Sessions multiple "+ids.size());
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            Map<Integer, List<SessionData>> sessions = new HashMap<>();
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            for (Integer id : ids) {
                sessions.put(id, new ArrayList<>());
            }
            while (set.next()) {
                Integer id = set.getInt(columnUserID);
                if (!ids.contains(id)) {
                    continue;
                }
                sessions.get(id).add(new SessionData(set.getLong(columnSessionStart), set.getLong(columnSessionEnd)));
            }
            set.close();
            statement.close();

            return sessions;
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("Get Sessions multiple "+ids.size());
        }
    }

    /**
     *
     * @param sessions
     * @throws SQLException
     */
    public void saveSessionData(Map<Integer, List<SessionData>> sessions) throws SQLException {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        Benchmark.start("Save Sessions multiple "+sessions.size());
        Map<Integer, List<SessionData>> saved = getSessionData(sessions.keySet());
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnUserID + ", "
                    + columnSessionStart + ", "
                    + columnSessionEnd
                    + ") VALUES (?, ?, ?)");

            boolean commitRequired = false;
            for (Integer id : sessions.keySet()) {
                List<SessionData> sessionList = sessions.get(id);
                List<SessionData> s = saved.get(id);
                if (s != null) {
                    sessionList.removeAll(s);
                }
                if (sessionList.isEmpty()) {
                    continue;
                }
                for (SessionData session : sessionList) {
                    long end = session.getSessionEnd();
                    long start = session.getSessionStart();
                    if (end < start) {
                        continue;
                    }
                    statement.setInt(1, id);
                    statement.setLong(2, start);
                    statement.setLong(3, end);
                    statement.addBatch();
                    commitRequired = true;
                }
            }
            if (commitRequired) {
                statement.executeBatch();
            }
        } finally {
            close(statement);
            Benchmark.start("Save Sessions multiple "+sessions.size());
        }
    }
}
