package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table class representing database table plan_world_times.
 *
 * @author Rsl1122
 * @since 3.6.0 / Database version 7
 */
public class WorldTimesTable extends UserIDTable {

    private final String columnServerID = "server_id"; //TODO
    private final WorldTable worldTable;
    private final String worldIDColumn;
    private final String worldNameColumn;

    private final String columnWorldId = "world_id";
    @Deprecated
    private final String columnPlaytime = "playtime";
    //TODO GM Times to World table

    private final String selectWorldIDsql;

    /**
     * Constructor.
     *
     * @param db         Database this table is a part of.
     * @param usingMySQL Database is a MySQL database.
     */
    public WorldTimesTable(SQLDB db, boolean usingMySQL) {
        super("plan_world_times", db, usingMySQL);
        worldTable = db.getWorldTable();
        worldIDColumn = worldTable + "." + worldTable.getColumnID();
        worldNameColumn = worldTable.getColumnWorldName();

        selectWorldIDsql = "(SELECT " + worldIDColumn + " FROM " + worldTable + " WHERE (" + worldNameColumn + "=?))";
    }

    @Override
    public boolean createTable() {
        UsersTable usersTable = db.getUsersTable();
        try {
            execute(TableSqlParser.createTable(tableName)
                    .column(columnUserID, Sql.INT).notNull()
                    .column(columnWorldId, Sql.INT).notNull()
                    .column(columnPlaytime, Sql.LONG).notNull()
                    .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                    .foreignKey(columnWorldId, worldTable.getTableName(), worldTable.getColumnID())
                    .toString()
            );
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    public boolean removeUserWorldTimes(int userId) {
        return super.removeDataOf(userId);
    }

    /**
     * @param userId
     * @return
     * @throws SQLException
     */
    public Map<String, Long> getWorldTimes(int userId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT "
                    + columnPlaytime + ", "
                    + worldNameColumn
                    + " FROM " + tableName + ", " + worldTable
                    + " WHERE (" + columnUserID + "=?)"
                    + " AND (" + worldIDColumn + "=" + tableName + "." + columnWorldId + ")");
            statement.setInt(1, userId);
            set = statement.executeQuery();
            HashMap<String, Long> times = new HashMap<>();
            while (set.next()) {
                times.put(set.getString(worldNameColumn), set.getLong(columnPlaytime));
            }
            return times;
        } finally {
            close(set);
            close(statement);
        }
    }

    public Map<Integer, Map<String, Long>> getWorldTimes(Collection<Integer> userIds) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        Map<Integer, Map<String, Long>> times = new HashMap<>();
        for (Integer id : userIds) {
            times.put(id, new HashMap<>());
        }
        try {
            statement = prepareStatement("SELECT "
                    + columnUserID + ", "
                    + columnPlaytime + ", "
                    + worldNameColumn
                    + " FROM " + tableName + ", " + worldTable
                    + " WHERE (" + worldIDColumn + "=" + tableName + "." + columnWorldId + ")");
            set = statement.executeQuery();
            while (set.next()) {
                int id = set.getInt(columnUserID);
                if (!userIds.contains(id)) {
                    continue;
                }
                Map<String, Long> worldTimes = times.get(id);
                worldTimes.put(set.getString(worldNameColumn), set.getLong(columnPlaytime));
            }
            return times;
        } finally {
            close(set);
            close(statement);
        }
    }

    public void saveWorldTimes(int userId, Map<String, Long> worldTimes) throws SQLException {
        if (Verify.isEmpty(worldTimes)) {
            return;
        }
        Map<String, Long> saved = getWorldTimes(userId);

        Map<String, Long> newData = new HashMap<>();
        Map<String, Long> updateData = new HashMap<>();

        for (Map.Entry<String, Long> entry : worldTimes.entrySet()) {
            String world = entry.getKey();
            long time = entry.getValue();
            Long savedTime = saved.get(world);

            if (savedTime == null) {
                newData.put(world, time);
            } else {
                if (savedTime < time) {
                    updateData.put(world, time);
                }
            }
        }
        insertWorlds(userId, newData);
        updateWorlds(userId, updateData);
    }

    private void updateWorlds(int userId, Map<String, Long> updateData) throws SQLException {
        if (Verify.isEmpty(updateData)) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(
                    "UPDATE " + tableName + " SET " + columnPlaytime + "=?" +
                            " WHERE (" + selectWorldIDsql + "=" + columnWorldId + ")" +
                            " AND (" + columnUserID + "=?)"
            );
            boolean commitRequired = false;
            for (Map.Entry<String, Long> entry : updateData.entrySet()) {
                String worldName = entry.getKey();
                long time = entry.getValue();
                statement.setLong(1, time);
                statement.setString(2, worldName);
                statement.setInt(3, userId);
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

    private void insertWorlds(int userId, Map<String, Long> newData) throws SQLException {
        if (Verify.isEmpty(newData)) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(
                    "INSERT INTO " + tableName + " ("
                            + columnUserID + ", "
                            + columnWorldId + ", "
                            + columnPlaytime
                            + ") VALUES (?, " + selectWorldIDsql + ", ?)"
            );
            boolean commitRequired = false;
            for (Map.Entry<String, Long> entry : newData.entrySet()) {
                String worldName = entry.getKey();
                long time = entry.getValue();
                statement.setInt(1, userId);
                statement.setString(2, worldName);
                statement.setLong(3, time);
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

    public void saveWorldTimes(Map<Integer, Map<String, Long>> worldTimesMultiple) throws SQLException {
        if (Verify.isEmpty(worldTimesMultiple)) {
            return;
        }
        Map<Integer, Map<String, Long>> saved = getWorldTimes(worldTimesMultiple.keySet());

        Map<Integer, Map<String, Long>> newData = new HashMap<>();
        Map<Integer, Map<String, Long>> updateData = new HashMap<>();

        for (Map.Entry<Integer, Map<String, Long>> entry : worldTimesMultiple.entrySet()) {
            int userId = entry.getKey();
            Map<String, Long> savedTimes = saved.get(userId);
            Map<String, Long> worldTimes = entry.getValue();
            Map<String, Long> newTimes = new HashMap<>(worldTimes);
            newTimes.keySet().removeAll(savedTimes.keySet());

            newData.put(userId, newTimes);

            for (Map.Entry<String, Long> times : savedTimes.entrySet()) {
                String world = times.getKey();
                long savedTime = times.getValue();
                Long toSave = worldTimes.get(world);
                if (toSave != null && toSave <= savedTime) {
                    worldTimes.remove(world);
                }
            }
            updateData.put(userId, worldTimes);
        }
        insertWorlds(newData);
        updateWorlds(updateData);
    }

    private void updateWorlds(Map<Integer, Map<String, Long>> updateData) throws SQLException {
        if (Verify.isEmpty(updateData)) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(
                    "UPDATE " + tableName + " SET " + columnPlaytime + "=?" +
                            " WHERE (" + selectWorldIDsql + "=" + columnWorldId + ")" +
                            " AND (" + columnUserID + "=?)"
            );
            boolean commitRequired = false;
            for (Map.Entry<Integer, Map<String, Long>> entry : updateData.entrySet()) {
                int userId = entry.getKey();
                for (Map.Entry<String, Long> times : entry.getValue().entrySet()) {
                    String worldName = times.getKey();
                    long time = times.getValue();
                    statement.setLong(1, time);
                    statement.setString(2, worldName);
                    statement.setInt(3, userId);
                    statement.addBatch();
                    commitRequired = true;
                }
            }
            if (commitRequired) {
                statement.executeBatch();
            }
        } finally {
            close(statement);
        }
    }

    private void insertWorlds(Map<Integer, Map<String, Long>> newData) throws SQLException {
        if (Verify.isEmpty(newData)) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(
                    "INSERT INTO " + tableName + " ("
                            + columnUserID + ", "
                            + columnWorldId + ", "
                            + columnPlaytime
                            + ") VALUES (?, " + selectWorldIDsql + ", ?)"
            );
            boolean commitRequired = false;
            for (Map.Entry<Integer, Map<String, Long>> entry : newData.entrySet()) {
                int userId = entry.getKey();
                for (Map.Entry<String, Long> times : entry.getValue().entrySet()) {
                    String worldName = times.getKey();
                    long time = times.getValue();
                    statement.setInt(1, userId);
                    statement.setString(2, worldName);
                    statement.setLong(3, time);
                    statement.addBatch();
                    commitRequired = true;
                }
            }
            if (commitRequired) {
                statement.executeBatch();
            }
        } finally {
            close(statement);
        }
    }

    public void saveWorldTimes(long sessionID, WorldTimes worldTimes) {
        // TODO saveWorldTimes (INSERT)
    }

    public void addWorldTimesToSessions(UUID uuid, List<Session> allSessions) {
        // TODO saveWorldTimes
    }
}
