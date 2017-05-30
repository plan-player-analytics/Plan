package main.java.com.djrapitops.plan.database.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import org.bukkit.GameMode;

/**
 *
 * @author Rsl1122
 */
public class GMTimesTable extends Table {

    private final String columnUserID;
    private final String columnSurvivalTime;
    private final String columnCreativeTime;
    private final String columnAdventureTime;
    private final String columnSpectatorTime;

    /**
     *
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

    /**
     *
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
     *
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
     *
     * @param userId
     * @return
     * @throws SQLException
     */
    public HashMap<GameMode, Long> getGMTimes(int userId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE (" + columnUserID + "=?)");
            statement.setInt(1, userId);
            set = statement.executeQuery();
            HashMap<GameMode, Long> times = new HashMap<>();
            while (set.next()) {
                times.put(GameMode.SURVIVAL, set.getLong(columnSurvivalTime));
                times.put(GameMode.CREATIVE, set.getLong(columnCreativeTime));
                times.put(GameMode.ADVENTURE, set.getLong(columnAdventureTime));
                try {
                    times.put(GameMode.SPECTATOR, set.getLong(columnSpectatorTime));
                } catch (NoSuchFieldError e) {
                }
            }
            return times;
        } finally {
            close(set);
            close(statement);
        }
    }

    /**
     *
     * @param userId
     * @param gamemodeTimes
     * @throws SQLException
     */
    public void saveGMTimes(int userId, Map<GameMode, Long> gamemodeTimes) throws SQLException {
        if (gamemodeTimes == null || gamemodeTimes.isEmpty()) {
            return;
        }
        
        PreparedStatement statement = null;
        GameMode[] gms = new GameMode[]{GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE, GameMode.SPECTATOR};
        int update = 0;
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

    private void addNewGMTimesRow(int userId, Map<GameMode, Long> gamemodeTimes) throws SQLException {
        PreparedStatement statement = null;
        GameMode[] gms = new GameMode[]{GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE, GameMode.SPECTATOR};
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
                    if (time != null) {
                        statement.setLong(i + 2, time);
                    } else {
                        statement.setLong(i + 2, 0);
                    }
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
