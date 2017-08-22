package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.database.Container;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;
import main.java.com.djrapitops.plan.utilities.Benchmark;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class SessionsTable extends UserIDTable {

    private final String columnSessionID; //TODO
    private final String columnSessionStart;
    private final String columnSessionEnd;
    private final String columnServerID; //TODO
    private final String columnMobKills; //TODO
    private final String columnDeaths; //TODO

    /**
     * @param db
     * @param usingMySQL
     */
    public SessionsTable(SQLDB db, boolean usingMySQL) {
        super("plan_sessions", db, usingMySQL);
        columnUserID = "user_id";
        columnSessionStart = "session_start";
        columnSessionEnd = "session_end";
        columnServerID = "server_id";
        columnSessionID = "id";
        columnMobKills = "mob_kills";
        columnDeaths = "deaths";
    }

    /**
     * @return
     */
    @Override
    public boolean createTable() {
        try {
            UsersTable usersTable = db.getUsersTable();
            execute(TableSqlParser.createTable(tableName)
                    .column(columnUserID, Sql.INT).notNull()
                    .column(columnSessionStart, Sql.LONG).notNull()
                    .column(columnSessionEnd, Sql.LONG).notNull()
                    .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                    .toString()
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
    public List<Session> getSessionData(int userId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE (" + columnUserID + "=?)");
            statement.setInt(1, userId);
            set = statement.executeQuery();
            List<Session> sessions = new ArrayList<>();
            while (set.next()) {
//                sessions.add(new Session(set.getLong(columnSessionStart), set.getLong(columnSessionEnd)));
            }
            set.close();
            statement.close();
            return sessions;
        } finally {
            close(set);
            close(statement);
        }
    }

    /**
     * @param userId
     * @return
     */
    public boolean removeUserSessions(int userId) {
        return super.removeDataOf(userId);
    }

    /**
     * @param userId
     * @param sessions
     * @throws SQLException
     */
    public void saveSessionData(int userId, List<Session> sessions) throws SQLException {
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
            for (Session session : sessions) {
                long end = session.getSessionEnd();
                long start = session.getSessionStart();
                if (end < start) {
                    continue;
                }

                statement.setInt(1, userId);
                statement.setLong(2, start);
                statement.setLong(3, end);
                statement.addBatch();
            }

            statement.executeBatch();
        } finally {
            close(statement);
        }
    }

    /**
     * @param ids
     * @return
     * @throws SQLException
     */
    public Map<Integer, List<Session>> getSessionData(Collection<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }

        Benchmark.start("Get Sessions multiple");
        PreparedStatement statement = null;
        ResultSet set = null;

        try {
            Map<Integer, List<Session>> sessions = new HashMap<>();
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

                long sessionStart = set.getLong(columnSessionStart);
                long sessionEnd = set.getLong(columnSessionEnd);

//                sessions.get(id).add(new Session(sessionStart, sessionEnd));
            }

            return sessions;
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("Database", "Get Sessions multiple");
        }
    }

    /**
     * @param sessions
     * @throws SQLException
     */
    public void saveSessionData(Map<Integer, List<Session>> sessions) throws SQLException {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        Benchmark.start("Save Sessions multiple");

        Map<Integer, List<Session>> saved = getSessionData(sessions.keySet());
        for (Map.Entry<Integer, List<Session>> entrySet : sessions.entrySet()) {
            Integer id = entrySet.getKey();
            List<Session> sessionList = entrySet.getValue();
            List<Session> s = saved.get(id);

            if (s != null) {
                sessionList.removeAll(s);
            }

            if (sessionList.isEmpty()) {
                continue;
            }

            saved.put(id, sessionList);
        }

        List<List<Container<Session>>> batches = splitIntoBatches(sessions);

        batches.forEach(batch -> {
            try {
                saveSessionBatch(batch);
            } catch (SQLException e) {
                Log.toLog("SessionsTable.saveSessionData", e);
            }
        });

        Benchmark.stop("Database", "Save Sessions multiple");
    }

    private void saveSessionBatch(List<Container<Session>> batch) throws SQLException {
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

            for (Container<Session> data : batch) {
                Session session = data.getObject();
                int id = data.getId();
                statement.setInt(1, id);
                statement.setLong(2, session.getSessionStart());
                statement.setLong(3, session.getSessionEnd());
                statement.addBatch();
            }
            statement.executeBatch();
        } finally {
            close(statement);
        }
    }

    public void clean() {
        // TODO Clean sessions before Configurable time span
    }
}
