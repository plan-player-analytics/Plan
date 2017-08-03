package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.database.Container;
import main.java.com.djrapitops.plan.database.DBUtils;
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
        if (Verify.isEmpty(gamemodeTimes)) {
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
                Long time = gamemodeTimes.get(gms[i]);
                statement.setLong(i + 1, time != null ? time : 0);
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
        if (Verify.isEmpty(gamemodeTimes)) {
            return;
        }

        Benchmark.start("Database: Save GMTimes");

        Set<Integer> savedIDs = getSavedIDs();

        Map<Integer, GMTimes> gmTimes = new HashMap<>();

        for (Map.Entry<Integer, Map<String, Long>> entrySet : gamemodeTimes.entrySet()) {
            int userID = entrySet.getKey();

            if (!savedIDs.contains(userID)) {
                continue;
            }

            Map<String, Long> gmTimesMap = entrySet.getValue();
            gmTimes.put(userID, new GMTimes(gmTimesMap));
        }

        List<List<Container<GMTimes>>> batches = DBUtils.splitIntoBatchesWithID(gmTimes);

        batches.stream().forEach(batch -> {
            try {
                saveGMTimesBatch(batch);
            } catch (SQLException e) {
                Log.toLog("GMTimesTable.saveGMTimes", e);
            }
        });

        gamemodeTimes.keySet().removeAll(savedIDs);

        addNewGMTimesRows(gamemodeTimes);
        Benchmark.stop("Database: Save GMTimes");
    }

    private void saveGMTimesBatch(List<Container<GMTimes>> batch) throws SQLException {
        if (batch.isEmpty()) {
            return;
        }

        int batchSize = batch.size();
        Log.debug("Preparing insertion of GM Times... Batch Size: " + batchSize);

        String[] gms = getGMKeyArray();
        Set<Integer> savedIDs = getSavedIDs();

        PreparedStatement statement = null;
        try {
            statement = prepareStatement(
                    "UPDATE " + tableName + " SET "
                            + columnSurvivalTime + "=?, "
                            + columnCreativeTime + "=?, "
                            + columnAdventureTime + "=?, "
                            + columnSpectatorTime + "=? "
                            + " WHERE (" + columnUserID + "=?)");

            for (Container<GMTimes> data : batch) {
                int id = data.getId();

                if (!savedIDs.contains(id)) {
                    continue;
                }

                statement.setInt(5, id);

                for (int i = 0; i < gms.length; i++) {
                    Map<String, Long> times = data.getObject().getTimes();
                    Long time = times.get(gms[i]);

                    statement.setLong(i + 1, time != null ? time : 0);
                }

                statement.addBatch();
            }

            Log.debug("Executing GM Times batch: " + batchSize);
            statement.executeBatch();
        } finally {
            close(statement);
        }
    }

    private void addNewGMTimesRows(Map<Integer, Map<String, Long>> gamemodeTimes) throws SQLException {
        if (Verify.isEmpty(gamemodeTimes)) {
            return;
        }

        Benchmark.start("Database: Add GMTimes Rows");

        Map<Integer, GMTimes> gmTimes = new HashMap<>();

        for (Map.Entry<Integer, Map<String, Long>> entrySet : gamemodeTimes.entrySet()) {
            int userID = entrySet.getKey();
            Map<String, Long> gmTimesMap = entrySet.getValue();
            gmTimes.put(userID, new GMTimes(gmTimesMap));
        }

        List<List<Container<GMTimes>>> batches = DBUtils.splitIntoBatchesWithID(gmTimes);

        batches.forEach(batch -> {
            try {
                addNewGMTimesBatch(batch);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        Benchmark.stop("Database: Add GMTimes Rows");
    }

    private void addNewGMTimesBatch(List<Container<GMTimes>> batch) throws SQLException {
        if (batch.isEmpty()) {
            return;
        }

        int batchSize = batch.size();
        Log.debug("Preparing insertion of GM Times... Batch Size: " + batchSize);

        String[] gms = getGMKeyArray();

        PreparedStatement statement = null;
        try {
            statement = prepareStatement(
                    "INSERT INTO " + tableName + " ("
                            + columnUserID + ", "
                            + columnSurvivalTime + ", "
                            + columnCreativeTime + ", "
                            + columnAdventureTime + ", "
                            + columnSpectatorTime
                            + ") VALUES (?, ?, ?, ?, ?)");

            for (Container<GMTimes> data : batch) {
                statement.setInt(1, data.getId());

                for (int i = 0; i < gms.length; i++) {
                    Map<String, Long> times = data.getObject().getTimes();
                    Long time = times.get(gms[i]);

                    statement.setLong(i + 2, time != null ? time : 0);
                }

                statement.addBatch();
            }

            Log.debug("Executing GM Times batch: " + batchSize);
            statement.executeBatch();
        } finally {
            close(statement);
        }
    }

    private void addNewGMTimesRow(int userId, Map<String, Long> gamemodeTimes) throws SQLException {
        if (Verify.isEmpty(gamemodeTimes)) {
            return;
        }

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
                Long time = gamemodeTimes.get(gms[i]);
                statement.setLong(i + 2, time != null ? time : 0);
            }

            statement.execute();
        } finally {
            close(statement);
        }
    }
}
