package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.KillData;
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
public class KillsTable extends Table {

    private final String columnKillerUserID;
    private final String columnVictimUserID;
    private final String columnWeapon;
    private final String columnDate;

    /**
     * @param db
     * @param usingMySQL
     */
    public KillsTable(SQLDB db, boolean usingMySQL) {
        super("plan_kills", db, usingMySQL);
        columnWeapon = "weapon";
        columnDate = "date";
        columnKillerUserID = "killer_id";
        columnVictimUserID = "victim_id";
    }

    /**
     * @return
     */
    @Override
    public boolean createTable() {
        UsersTable usersTable = db.getUsersTable();
        try {
            execute(TableSqlParser.createTable(tableName)
                    .column(columnKillerUserID, Sql.INT).notNull()
                    .column(columnVictimUserID, Sql.INT).notNull()
                    .column(columnWeapon, Sql.VARCHAR(30)).notNull()
                    .column(columnDate, Sql.LONG).notNull()
                    .foreignKey(columnKillerUserID, usersTable.getTableName(), usersTable.getColumnID())
                    .foreignKey(columnVictimUserID, usersTable.getTableName(), usersTable.getColumnID())
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
    public boolean removeUserKillsAndVictims(int userId) {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE " + columnKillerUserID + " = ? OR " + columnVictimUserID + " = ?");
            statement.setInt(1, userId);
            statement.setInt(2, userId);
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
    public List<KillData> getPlayerKills(int userId) throws SQLException {
        UsersTable usersTable = db.getUsersTable();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE (" + columnKillerUserID + "=?)");
            statement.setInt(1, userId);
            set = statement.executeQuery();
            List<KillData> killData = new ArrayList<>();
            while (set.next()) {
                int victimID = set.getInt(columnVictimUserID);
                UUID victimUUID = usersTable.getUserUUID(String.valueOf(victimID));
                killData.add(new KillData(victimUUID, victimID, set.getString(columnWeapon), set.getLong(columnDate)));
            }
            return killData;
        } finally {
            close(set);
            close(statement);
        }
    }

    /**
     * @param userId
     * @param kills
     * @throws SQLException
     */
    public void savePlayerKills(int userId, List<KillData> kills) throws SQLException {
        if (Verify.isEmpty(kills)) {
            return;
        }
        Benchmark.start("Save Kills");
        kills.removeAll(getPlayerKills(userId));
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnKillerUserID + ", "
                    + columnVictimUserID + ", "
                    + columnWeapon + ", "
                    + columnDate
                    + ") VALUES (?, ?, ?, ?)");
            boolean commitRequired = false;
            kills.stream().filter(Objects::nonNull).forEach(killData -> {
                int victimUserID = killData.getVictimUserID();
                if (victimUserID == -1) {
                    try {
                        int newVictimID = db.getUsersTable().getUserId(killData.getVictim());
                        killData.setVictimUserID(newVictimID);
                    } catch (SQLException e) {
                        Log.toLog(this.getClass().getName(), e);
                        return;
                    }
                }
            });
            for (KillData kill : kills) {
                if (kill == null || kill.getVictimUserID() == -1) {
                    continue;
                }
                statement.setInt(1, userId);
                statement.setInt(2, kill.getVictimUserID());
                statement.setString(3, kill.getWeapon());
                statement.setLong(4, kill.getDate());
                statement.addBatch();
                commitRequired = true;
            }

            if (commitRequired) {
                statement.executeBatch();
            }
        } finally {
            close(statement);
            Benchmark.stop("Database", "Save Kills");
        }
    }

    /**
     * @param ids
     * @param uuids
     * @return
     * @throws SQLException
     */
    public Map<Integer, List<KillData>> getPlayerKills(Collection<Integer> ids, Map<Integer, UUID> uuids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        Benchmark.start("Get Kills multiple");
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            Map<Integer, List<KillData>> kills = new HashMap<>();
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            for (Integer id : ids) {
                kills.put(id, new ArrayList<>());
            }
            while (set.next()) {
                int killerID = set.getInt(columnKillerUserID);
                int victimID = set.getInt(columnVictimUserID);
                if (!ids.contains(killerID)) {
                    continue;
                }
                UUID victimUUID = uuids.get(victimID);
                kills.get(killerID).add(new KillData(victimUUID, victimID, set.getString(columnWeapon), set.getLong(columnDate)));
            }
            return kills;
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("Database", "Get Kills multiple");
        }
    }

    /**
     * @param kills
     * @param uuids
     * @throws SQLException
     */
    public void savePlayerKills(Map<Integer, List<KillData>> kills, Map<Integer, UUID> uuids) throws SQLException {
        if (kills == null || kills.isEmpty()) {
            return;
        }

        Benchmark.start("Save Kills multiple");
        Map<Integer, List<KillData>> saved = getPlayerKills(kills.keySet(), uuids);

        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnKillerUserID + ", "
                    + columnVictimUserID + ", "
                    + columnWeapon + ", "
                    + columnDate
                    + ") VALUES (?, ?, ?, ?)");
            boolean commitRequired = false;
            for (Map.Entry<Integer, List<KillData>> entrySet : kills.entrySet()) {
                Integer id = entrySet.getKey();
                List<KillData> playerKills = entrySet.getValue();

                List<KillData> s = saved.get(id);

                if (s != null) {
                    playerKills.removeAll(s);
                }

                playerKills.stream().filter(Objects::nonNull).forEach(killData -> {
                    int victimUserID = killData.getVictimUserID();
                    if (victimUserID == -1) {
                        try {
                            int newVictimID = db.getUsersTable().getUserId(killData.getVictim());
                            killData.setVictimUserID(newVictimID);
                        } catch (SQLException e) {
                            Log.toLog(this.getClass().getName(), e);
                            return;
                        }
                    }
                });
                for (KillData kill : playerKills) {
                    if (kill == null || kill.getVictimUserID() == -1) {
                        continue;
                    }
                    statement.setInt(1, id);
                    statement.setInt(2, kill.getVictimUserID());
                    statement.setString(3, kill.getWeapon());
                    statement.setLong(4, kill.getDate());
                    statement.addBatch();
                    commitRequired = true;
                }

                if (commitRequired) {
                    statement.executeBatch();
                }
            }
        } finally {
            close(statement);
            Benchmark.stop("Database", "Save Kills multiple");
        }
    }
}
