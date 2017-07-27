package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.database.Container;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.ManageUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class SessionsTable extends Table {

    private final String columnUserID;
    private final String columnSessionStart;
    private final String columnSessionEnd;

    /**
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
     * @param userId
     * @return
     * @throws SQLException
     */
    public List<SessionData> getSessionData(int userId) throws SQLException {
        Benchmark.start("Database: Get Sessions");
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
            Benchmark.stop("Database: Get Sessions");
        }
    }

    /**
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
     * @param userId
     * @param sessions
     * @throws SQLException
     */
    public void saveSessionData(int userId, List<SessionData> sessions) throws SQLException {
        if (sessions == null) {
            return;
        }
        Benchmark.start("Database: Save Sessions");
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
            Benchmark.stop("Database: Save Sessions");
        }
    }

    /**
     * @param ids
     * @return
     * @throws SQLException
     */
    public Map<Integer, List<SessionData>> getSessionData(Collection<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        Benchmark.start("Database: Get Sessions multiple");
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
            Benchmark.stop("Database: Get Sessions multiple");
        }
    }

    /**
     * @param sessions
     * @throws SQLException
     */
    public void saveSessionData(Map<Integer, List<SessionData>> sessions) throws SQLException {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        Benchmark.start("Database: Save Sessions multiple");
        Map<Integer, List<SessionData>> saved = getSessionData(sessions.keySet());
        for (Integer id : sessions.keySet()) {
            List<SessionData> sessionList = sessions.get(id);
            List<SessionData> s = saved.get(id);
            if (s != null) {
                sessionList.removeAll(s);
            }
            if (sessionList.isEmpty()) {
                continue;
            }
            saved.put(id, sessionList);
        }
        List<List<Container<SessionData>>> batches = splitIntoBatches(sessions);
        for (List<Container<SessionData>> batch : batches) {
            saveSessionBatch(batch);
        }
        Benchmark.stop("Database: Save Sessions multiple");
    }

    private void saveSessionBatch(List<Container<SessionData>> batch) throws SQLException {
        if (batch.isEmpty()) {
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
            int i = 0;
            for (Container<SessionData> data : batch) {
                SessionData session = data.getObject();
                int id = data.getId();
                if (!session.isValid()) {
                    continue;
                }
                statement.setInt(1, id);
                statement.setLong(2, session.getSessionStart());
                statement.setLong(3, session.getSessionEnd());
                statement.addBatch();
                commitRequired = true;
                i++;
            }
            if (commitRequired) {
                Log.debug("Executing session batch: " + i);
                statement.executeBatch();
            }
        } finally {
            close(statement);
        }
    }

    /**
     * @throws SQLException
     */
    public void clean() throws SQLException {
        Map<Integer, Integer> loginTimes = db.getUsersTable().getLoginTimes();
        Map<Integer, List<SessionData>> allSessions = getSessionData(loginTimes.keySet());
        Benchmark.start("Database: Combine Sessions");
        int before = MathUtils.sumInt(allSessions.values().stream().map(List::size));
        Log.debug("Sessions before: " + before);
        Map<Integer, Integer> beforeM = new HashMap<>();
        Map<Integer, Integer> afterM = new HashMap<>();
        for (Integer id : allSessions.keySet()) {
            List<SessionData> sessions = allSessions.get(id);
            beforeM.put(id, sessions.size());
            if (sessions.isEmpty()) {
                afterM.put(id, 0);
                continue;
            }
            Integer times = loginTimes.get(id);
            if (sessions.size() == times) {
                afterM.put(id, times);
                continue;
            }
            List<SessionData> combined = ManageUtils.combineSessions(sessions, times);
            afterM.put(id, combined.size());
            allSessions.put(id, combined);
        }
        int after = MathUtils.sumInt(allSessions.values().stream().map(List::size));
        Log.debug("Sessions after: " + after);
        if (before - after > 50) {
            Benchmark.start("Database: Save combined sessions");
            for (Integer id : new HashSet<>(allSessions.keySet())) {
                if (afterM.get(id) < beforeM.get(id)) {
                    removeUserSessions(id);
                } else {
                    allSessions.remove(id);
                }
            }
            saveSessionData(allSessions);
            Benchmark.stop("Database: Save combined sessions");
        }
        Benchmark.stop("Database: Combine Sessions");
        Log.info("Combined " + (before - after) + " sessions.");
    }
}
