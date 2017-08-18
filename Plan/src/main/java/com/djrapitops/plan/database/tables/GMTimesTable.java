package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.database.Container;
import main.java.com.djrapitops.plan.database.DBUtils;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class GMTimesTable extends UserIDTable {

    private final String columnSurvivalTime;
    private final String columnCreativeTime;
    private final String columnAdventureTime;
    private final String columnSpectatorTime;

    private static final String SURVIVAL = "SURVIVAL";
    private static final String CREATIVE = "CREATIVE";
    private static final String ADVENTURE = "ADVENTURE";
    private static final String SPECTATOR = "SPECTATOR";


    /**
     * @param db
     * @param usingMySQL
     */
    public GMTimesTable(SQLDB db, boolean usingMySQL) {
        super("plan_gamemodetimes", db, usingMySQL);
        columnUserID = "user_id";
        columnSurvivalTime = "SURVIVAL";
        columnCreativeTime = "CREATIVE";
        columnAdventureTime = "ADVENTURE";
        columnSpectatorTime = "SPECTATOR";
    }

    public static String[] getGMKeyArray() {
        return new String[]{SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR};
    }

    /**
     * @return
     */
    @Override
    public boolean createTable() {
        UsersTable usersTable = db.getUsersTable();
        try {
            execute(TableSqlParser.createTable(tableName)
                    .column(columnUserID, Sql.INT).notNull()
                    .column(columnSurvivalTime, Sql.LONG).notNull()
                    .column(columnCreativeTime, Sql.LONG).notNull()
                    .column(columnAdventureTime, Sql.LONG).notNull()
                    .column(columnSpectatorTime, Sql.LONG).notNull()
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
     */
    public boolean removeUserGMTimes(int userId) {
        return super.removeDataOf(userId);
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
                times.put(SURVIVAL, set.getLong(columnSurvivalTime));
                times.put(CREATIVE, set.getLong(columnCreativeTime));
                times.put(ADVENTURE, set.getLong(columnAdventureTime));
                times.put(SPECTATOR, set.getLong(columnSpectatorTime));
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

                gmTimes.put(SURVIVAL, set.getLong(columnSurvivalTime));
                gmTimes.put(CREATIVE, set.getLong(columnCreativeTime));
                gmTimes.put(ADVENTURE, set.getLong(columnAdventureTime));
                gmTimes.put(SPECTATOR, set.getLong(columnSpectatorTime));
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

        batches.forEach(batch -> {
            try {
                saveGMTimesBatch(batch);
            } catch (SQLException e) {
                Log.toLog("GMTimesTable.saveGMTimes", e);
            }
        });

        gamemodeTimes.keySet().removeAll(savedIDs);

        addNewGMTimesRows(gamemodeTimes);
    }

    private void saveGMTimesBatch(List<Container<GMTimes>> batch) throws SQLException {
        if (batch.isEmpty()) {
            return;
        }

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

            statement.executeBatch();
        } finally {
            close(statement);
        }
    }

    private void addNewGMTimesRows(Map<Integer, Map<String, Long>> gamemodeTimes) {
        if (Verify.isEmpty(gamemodeTimes)) {
            return;
        }

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
                Log.toLog("GMTimesTable.addNewGMTimesRows", e);
            }
        });
    }

    private void addNewGMTimesBatch(List<Container<GMTimes>> batch) throws SQLException {
        if (batch.isEmpty()) {
            return;
        }

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
