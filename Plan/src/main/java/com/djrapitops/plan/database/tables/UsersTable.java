package main.java.com.djrapitops.plan.database.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.api.Gender;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.UUIDFetcher;
import org.bukkit.GameMode;

/**
 *
 * @author Rsl1122
 */
public class UsersTable extends Table {

    private final String columnID;
    private final String columnUUID;
    private final String columnDemAge;
    private final String columnDemGender;
    private final String columnDemGeoLocation;
    private final String columnLastGM;
    private final String columnLastGMSwapTime;
    private final String columnPlayTime;
    private final String columnLoginTimes;
    private final String columnLastPlayed;
    private final String columnPlayerKills;
    private final String columnDeaths;
    private final String columnMobKills;

    public UsersTable(SQLDB db, boolean usingMySQL) {
        super("plan_users", db, usingMySQL);
        columnID = "id";
        columnUUID = "uuid";
        columnDemAge = "age";
        columnDemGender = "gender";
        columnDemGeoLocation = "geolocation";
        columnLastGM = "last_gamemode";
        columnLastGMSwapTime = "last_gamemode_swap";
        columnPlayTime = "play_time";
        columnLoginTimes = "login_times";
        columnLastPlayed = "last_played";
        columnMobKills = "mob_kills";
        columnPlayerKills = "player_kills"; // Removed in 2.7.0
        columnDeaths = "deaths";
    }

    @Override
    public boolean createTable() {
        try {
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnID + " integer " + ((usingMySQL) ? "NOT NULL AUTO_INCREMENT" : "PRIMARY KEY") + ", "
                    + columnUUID + " varchar(36) NOT NULL UNIQUE, "
                    + columnDemAge + " integer NOT NULL, "
                    + columnDemGender + " varchar(8) NOT NULL, "
                    + columnDemGeoLocation + " varchar(50) NOT NULL, "
                    + columnLastGM + " varchar(15) NOT NULL, "
                    + columnLastGMSwapTime + " bigint NOT NULL, "
                    + columnPlayTime + " bigint NOT NULL, "
                    + columnLoginTimes + " integer NOT NULL, "
                    + columnLastPlayed + " bigint NOT NULL, "
                    + columnDeaths + " int NOT NULL, "
                    + columnMobKills + " int NOT NULL"
                    + (usingMySQL ? ", PRIMARY KEY (" + columnID + ")" : "")
                    + ")"
            );
            if (getVersion() < 3) {
                alterTablesV3();
            }
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    private void alterTablesV3() {
        String[] queries;
        if (usingMySQL) {
            queries = new String[]{
                "ALTER TABLE " + tableName + " ADD " + columnDeaths + " integer NOT NULL DEFAULT 0",
                "ALTER TABLE " + tableName + " ADD " + columnMobKills + " integer NOT NULL DEFAULT 0",
                "ALTER TABLE " + tableName + " DROP INDEX " + columnPlayerKills
            };
        } else {
            queries = new String[]{
                "ALTER TABLE " + tableName + " ADD COLUMN " + columnDeaths + " integer NOT NULL DEFAULT 0",
                "ALTER TABLE " + tableName + " ADD COLUMN " + columnMobKills + " integer NOT NULL DEFAULT 0"
            };
        }
        for (String query : queries) {
            try {
                execute(query);
            } catch (Exception e) {
            }
        }
    }

    public int getUserId(UUID uuid) throws SQLException {
        return getUserId(uuid.toString());
    }

    public int getUserId(String uuid) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            int userId = -1;
            statement = prepareStatement("SELECT " + columnID + " FROM " + tableName + " WHERE (" + columnUUID + "=?)");
            statement.setString(1, uuid);
            set = statement.executeQuery();
            while (set.next()) {
                userId = set.getInt(columnID);
            }
            return userId;
        } finally {
            close(set);
            close(statement);
        }
    }

    public UUID getUserUUID(String userID) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            UUID uuid = null;
            statement = prepareStatement("SELECT " + columnUUID + " FROM " + tableName + " WHERE (" + columnID + "=?)");
            statement.setString(1, userID);
            set = statement.executeQuery();
            while (set.next()) {
                uuid = UUID.fromString(set.getString(columnUUID));
            }
            return uuid;
        } finally {
            close(set);
            close(statement);
        }
    }

    public Set<UUID> getSavedUUIDs() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            Set<UUID> uuids = new HashSet<>();
            statement = prepareStatement("SELECT " + columnUUID + " FROM " + tableName);
            set = statement.executeQuery();
            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString(columnUUID));
                if (uuid == null) {
                    continue;
                }
                uuids.add(uuid);
            }
            return uuids;
        } finally {
            close(set);
            close(statement);
        }
    }

    public boolean removeUser(UUID uuid) {
        return removeUser(uuid.toString());
    }

    public boolean removeUser(String uuid) {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE (" + columnUUID + "=?)");
            statement.setString(1, uuid);
            statement.execute();
            return true;
        } catch (SQLException ex) {
            return false;
        } finally {
            close(statement);
        }
    }

    public void addUserInformationToUserData(UserData data) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE (" + columnUUID + "=?)");
            statement.setString(1, data.getUuid().toString());
            set = statement.executeQuery();
            while (set.next()) {
                data.getDemData().setAge(set.getInt(columnDemAge));
                data.getDemData().setGender(Gender.parse(set.getString(columnDemGender)));
                data.getDemData().setGeoLocation(set.getString(columnDemGeoLocation));
                data.setLastGamemode(GameMode.valueOf(set.getString(columnLastGM)));
                data.setLastGmSwapTime(set.getLong(columnLastGMSwapTime));
                data.setPlayTime(set.getLong(columnPlayTime));
                data.setLoginTimes(set.getInt(columnLoginTimes));
                data.setLastPlayed(set.getLong(columnLastPlayed));
                data.setDeaths(set.getInt(columnDeaths));
                data.setMobKills(set.getInt(columnMobKills));
            }
        } finally {
            close(set);
            close(statement);
        }
    }

    public void saveUserDataInformation(UserData data) throws SQLException {
        PreparedStatement statement = null;
        try {
            UUID uuid = data.getUuid();
            int userId = getUserId(uuid);
            int update = 0;
            if (userId != -1) {
                String sql = "UPDATE " + tableName + " SET "
                        + columnDemAge + "=?, "
                        + columnDemGender + "=?, "
                        + columnDemGeoLocation + "=?, "
                        + columnLastGM + "=?, "
                        + columnLastGMSwapTime + "=?, "
                        + columnPlayTime + "=?, "
                        + columnLoginTimes + "=?, "
                        + columnLastPlayed + "=?, "
                        + columnDeaths + "=?, "
                        + columnMobKills + "=? "
                        + "WHERE UPPER(" + columnUUID + ") LIKE UPPER(?)";

                statement = prepareStatement(sql);
                statement.setInt(1, data.getDemData().getAge());
                statement.setString(2, data.getDemData().getGender().toString().toLowerCase());
                statement.setString(3, data.getDemData().getGeoLocation());
                GameMode gm = data.getLastGamemode();
                if (gm != null) {
                    statement.setString(4, data.getLastGamemode().name());
                } else {
                    statement.setString(4, GameMode.SURVIVAL.name());
                }
                statement.setLong(5, data.getLastGmSwapTime());
                statement.setLong(6, data.getPlayTime());
                statement.setInt(7, data.getLoginTimes());
                statement.setLong(8, data.getLastPlayed());
                statement.setInt(9, data.getDeaths());
                statement.setInt(10, data.getMobKills());
                statement.setString(11, uuid.toString());
                update = statement.executeUpdate();
            }
            if (update == 0) {
                close(statement);
                statement = prepareStatement("INSERT INTO " + tableName + " ("
                        + columnUUID + ", "
                        + columnDemAge + ", "
                        + columnDemGender + ", "
                        + columnDemGeoLocation + ", "
                        + columnLastGM + ", "
                        + columnLastGMSwapTime + ", "
                        + columnPlayTime + ", "
                        + columnLoginTimes + ", "
                        + columnLastPlayed + ", "
                        + columnDeaths + ", "
                        + columnMobKills
                        + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                statement.setString(1, uuid.toString());
                statement.setInt(2, data.getDemData().getAge());
                statement.setString(3, data.getDemData().getGender().toString().toLowerCase());
                statement.setString(4, data.getDemData().getGeoLocation());
                GameMode gm = data.getLastGamemode();
                if (gm != null) {
                    statement.setString(5, data.getLastGamemode().name());
                } else {
                    statement.setString(5, GameMode.SURVIVAL.name());
                }
                statement.setLong(6, data.getLastGmSwapTime());
                statement.setLong(7, data.getPlayTime());
                statement.setInt(8, data.getLoginTimes());
                statement.setLong(9, data.getLastPlayed());
                statement.setInt(10, data.getDeaths());
                statement.setInt(11, data.getMobKills());
                statement.execute();
            }
        } finally {
            close(statement);
        }
    }

    public List<UserData> saveUserDataInformationBatch(List<UserData> data) throws SQLException {
        PreparedStatement statement = null;
        try {
            List<UserData> saveLast = new ArrayList<>();
            String uSQL = "UPDATE " + tableName + " SET "
                    + columnDemAge + "=?, "
                    + columnDemGender + "=?, "
                    + columnDemGeoLocation + "=?, "
                    + columnLastGM + "=?, "
                    + columnLastGMSwapTime + "=?, "
                    + columnPlayTime + "=?, "
                    + columnLoginTimes + "=?, "
                    + columnLastPlayed + "=?, "
                    + columnDeaths + "=?, "
                    + columnMobKills + "=? "
                    + "WHERE " + columnUUID + "=?";
            statement = prepareStatement(uSQL);
            boolean commitRequired = false;
            Set<UUID> savedUUIDs = getSavedUUIDs();
            for (UserData uData : data) {
                try {
                    if (uData == null) {
                        continue;
                    }
                    UUID uuid = uData.getUuid();
                    if (uuid == null) {
                        try {
                            uData.setUuid(UUIDFetcher.getUUIDOf(uData.getName()));
                        } catch (Exception ex) {
                            continue;
                        }
                    }
                    uuid = uData.getUuid();
                    if (uuid == null) {
                        continue;
                    }
                    if (!savedUUIDs.contains(uuid)) {
                        saveLast.add(uData);
                        continue;
                    }
                    uData.access();
                    statement.setInt(1, uData.getDemData().getAge());
                    statement.setString(2, uData.getDemData().getGender().toString().toLowerCase());
                    statement.setString(3, uData.getDemData().getGeoLocation());
                    GameMode gm = uData.getLastGamemode();
                    if (gm != null) {
                        statement.setString(4, uData.getLastGamemode().name());
                    } else {
                        statement.setString(4, GameMode.SURVIVAL.name());
                    }
                    statement.setLong(5, uData.getLastGmSwapTime());
                    statement.setLong(6, uData.getPlayTime());
                    statement.setInt(7, uData.getLoginTimes());
                    statement.setLong(8, uData.getLastPlayed());
                    statement.setInt(9, uData.getDeaths());
                    statement.setInt(10, uData.getMobKills());
                    statement.setString(11, uData.getUuid().toString());
                    statement.addBatch();
                } catch (SQLException | NullPointerException e) {
                    saveLast.add(uData);
                    uData.stopAccessing();
                    continue;
                }
                uData.stopAccessing();
                commitRequired = true;
            }
            if (commitRequired) {
                statement.executeBatch();
            }
            return saveLast;
        } finally {
            close(statement);
        }
    }

    public String getColumnID() {
        return columnID;
    }
}
