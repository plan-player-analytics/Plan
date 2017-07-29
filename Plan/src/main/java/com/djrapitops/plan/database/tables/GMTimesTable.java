package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.Benchmark;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class GMTimesTable extends Table {

    private final String columnUserID;
    private final String columnSurvivalTime;
    private final String columnCreativeTime;
    private final String columnAdventureTime;
    private final String columnSpectatorTime;

    /**
     * @param db
     * @param usingMySQL
     */
    public GMTimesTable(SQLDB db, boolean usingMySQL) {
        super("plan_gamemodetimes", db, usingMySQL);
        columnUserID = "user_id";
        columnSurvivalTime = "survival";
        columnCreativeTime = "creative";
        columnAdventureTime = "adventure";
        columnSpectatorTime = "spectator";
    }

    public static String[] getGMKeyArray() {
        return new String[]{"SURVIVAL", "CREATIVE", "ADVENTURE", "SPECTATOR"};
    }

    /**
     * @return
     */
    @Override
    public boolean createTable() {
        UsersTable usersTable = db.getUsersTable();
        try {
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnUserID + " integer NOT NULL, "
                    + columnSurvivalTime + " bigint NOT NULL, "
                    + columnCreativeTime + " bigint NOT NULL, "
                    + columnAdventureTime + " bigint NOT NULL, "
                    + columnSpectatorTime + " bigint NOT NULL, "
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
     */
    public boolean removeUserGMTimes(int userId) {
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
     * @return
     * @throws SQLException
     */
    public Map<String, Long> getGMTimes(int userId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE (" + columnUserID + "=?)");
            statement.setInt(1, userId);
            set = statement.executeQuery();
            HashMap<String, Long> times = new HashMap<>();
            while (set.next()) {
                times.put("SURVIVAL", set.getLong(columnSurvivalTime));
                times.put("CREATIVE", set.getLong(columnCreativeTime));
                times.put("ADVENTURE", set.getLong(columnAdventureTime));
                times.put("SPECTATOR", set.getLong(columnSpectatorTime));
            }
            return times;
        } finally {
            close(set);
            close(statement);
        }
    }

    public Map<Integer, Map<String, Long>> getGMTimes(Collection<Integer> userIds) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        Map<Integer, Map<String, Long>> times = new HashMap<>();
        try {
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            while (set.next()) {
                Map<String, Long> gmTimes = new HashMap<>();
                int id = set.getInt(columnUserID);
                if (!userIds.contains(id)) {
                    continue;
                }
                gmTimes.put("SURVIVAL", set.getLong(columnSurvivalTime));
                gmTimes.put("CREATIVE", set.getLong(columnCreativeTime));
                gmTimes.put("ADVENTURE", set.getLong(columnAdventureTime));
                gmTimes.put("SPECTATOR", set.getLong(columnSpectatorTime));
                times.put(id, gmTimes);
            }
            return times;
        } finally {
            close(set);
            close(statement);
        }
    }

    /**
     * @param userId
     * @param gamemodeTimes
     * @throws SQLException
     */
    public void saveGMTimes(int userId, Map<String, Long> gamemodeTimes) throws SQLException {
        if (gamemodeTimes == null || gamemodeTimes.isEmpty()) {
            return;
        }
        PreparedStatement statement = null;
        String[] gms = getGMKeyArray();
        int update;
        try {
            statement = prepareStatement(
                    "UPDATE " + tableName + " SET "
                            + columnSurvivalTime + "=?, "
                            + columnCreativeTime + "=?, "
                            + columnAdventureTime + "=?, "
                            + columnSpectatorTime + "=? "
                            + " WHERE (" + columnUserID + "=?)");
            statement.setInt(5, userId);
            for (int i = 0; i < gms.length; i++) {
                try {
                    Long time = gamemodeTimes.get(gms[i]);
                    if (time != null) {
                        statement.setLong(i + 1, time);
                    } else {
                        statement.setLong(i + 1, 0);
                    }
                } catch (NoSuchFieldError e) {
                    statement.setLong(i + 1, 0);
                }
            }
            update = statement.executeUpdate();
        } finally {
            close(statement);
        }
        if (update == 0) {
            addNewGMTimesRow(userId, gamemodeTimes);
        }
    }

    private Set<Integer> getSavedIDs() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT " + columnUserID + " FROM " + tableName);
            set = statement.executeQuery();
            Set<Integer> ids = new HashSet<>();
            while (set.next()) {
                ids.add(set.getInt(columnUserID));
            }
            return ids;
        } finally {
            close(set);
            close(statement);
        }
    }

    public void saveGMTimes(Map<Integer, Map<String, Long>> gamemodeTimes) throws SQLException {
        if (gamemodeTimes == null || gamemodeTimes.isEmpty()) {
            return;
        }
        Benchmark.start("Database: Save GMTimes");
        PreparedStatement statement = null;
        String[] gms = getGMKeyArray();

        Set<Integer> savedIDs = getSavedIDs();
        try {
            statement = prepareStatement(
                    "UPDATE " + tableName + " SET "
                            + columnSurvivalTime + "=?, "
                            + columnCreativeTime + "=?, "
                            + columnAdventureTime + "=?, "
                            + columnSpectatorTime + "=? "
                            + " WHERE (" + columnUserID + "=?)");
            boolean commitRequired = false;
            for (Map.Entry<Integer, Map<String, Long>> entrySet : gamemodeTimes.entrySet()) {
                Integer id = entrySet.getKey();

                if (!savedIDs.contains(id)) {
                    continue;
                }

                statement.setInt(5, id);
                for (int i = 0; i < gms.length; i++) {
                    try {
                        Map<String, Long> times = entrySet.getValue();
                        Long time = times.get(gms[i]);

                        statement.setLong(i + 1, time != null ? time : 0);
                    } catch (NoSuchFieldError e) {
                        statement.setLong(i + 1, 0);
                    }
                }
                statement.addBatch();
                commitRequired = true;
            }

            if (commitRequired) {
                statement.executeBatch();
            }

            gamemodeTimes.keySet().removeAll(savedIDs);
        } finally {
            close(statement);
        }

        addNewGMTimesRows(gamemodeTimes);
        Benchmark.stop("Database: Save GMTimes");
    }

    private void addNewGMTimesRows(Map<Integer, Map<String, Long>> gamemodeTimes) throws SQLException {
        if (gamemodeTimes == null || gamemodeTimes.isEmpty()) {
            return;
        }
        PreparedStatement statement = null;
        String[] gms = getGMKeyArray();

        try {
            statement = prepareStatement(
                    "INSERT INTO " + tableName + " ("
                            + columnUserID + ", "
                            + columnSurvivalTime + ", "
                            + columnCreativeTime + ", "
                            + columnAdventureTime + ", "
                            + columnSpectatorTime
                            + ") VALUES (?, ?, ?, ?, ?)");
            boolean commitRequired = false;
            for (Map.Entry<Integer, Map<String, Long>> entry : gamemodeTimes.entrySet()) {
                Integer id = entry.getKey();

                statement.setInt(1, id);
                for (int i = 0; i < gms.length; i++) {
                    try {
                        Map<String, Long> times = entry.getValue();
                        Long time = times.get(gms[i]);

                        statement.setLong(i + 2, time != null ? time : 0);
                    } catch (NoSuchFieldError e) {
                        statement.setLong(i + 2, 0);
                    }
                }
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

    private void addNewGMTimesRow(int userId, Map<String, Long> gamemodeTimes) throws SQLException {
        PreparedStatement statement = null;
        String[] gms = getGMKeyArray();

        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnUserID + ", "
                    + columnSurvivalTime + ", "
                    + columnCreativeTime + ", "
                    + columnAdventureTime + ", "
                    + columnSpectatorTime
                    + ") VALUES (?, ?, ?, ?, ?)");

            statement.setInt(1, userId);
            for (int i = 0; i < gms.length; i++) {
                try {
                    Long time = gamemodeTimes.get(gms[i]);

                    statement.setLong(i + 2, time != null ? time : 0);
                } catch (NoSuchFieldError e) {
                    statement.setLong(i + 2, 0);
                }
            }

            statement.execute();
        } finally {
            close(statement);
        }
    }
}
