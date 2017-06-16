package main.java.com.djrapitops.plan.database.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.api.Gender;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;
import org.bukkit.GameMode;
import static org.bukkit.Bukkit.getOfflinePlayer;

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
    private final String columnRegistered;
    private final String columnOP;
    private final String columnName;
    private final String columnBanned;
    private final String columnContainsBukkitData;

    /**
     *
     * @param db
     * @param usingMySQL
     */
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
        // Added in 3.3.0
        columnRegistered = "registered";
        columnOP = "opped";
        columnName = "name";
        columnBanned = "banned";
        columnContainsBukkitData = "contains_bukkit_data";
    }

    /**
     *
     * @return
     */
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
                    + columnMobKills + " int NOT NULL, "
                    + columnRegistered + " bigint NOT NULL, "
                    + columnOP + " boolean NOT NULL DEFAULT 0, "
                    + columnName + " varchar(16) NOT NULL, "
                    + columnBanned + " boolean NOT NULL DEFAULT 0, "
                    + columnContainsBukkitData + " boolean NOT NULL DEFAULT 0"
                    + (usingMySQL ? ", PRIMARY KEY (" + columnID + ")" : "")
                    + ")"
            );
            int version = getVersion();
            if (version < 3) {
                alterTablesV3();
            }
            if (version < 4) {
                alterTablesV4();
            }
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    private void alterTablesV4() {
        String[] queries;
        if (usingMySQL) {
            queries = new String[]{
                "ALTER TABLE " + tableName + " ADD " + columnContainsBukkitData + " boolean NOT NULL DEFAULT 0",
                "ALTER TABLE " + tableName + " ADD " + columnOP + " boolean NOT NULL DEFAULT 0",
                "ALTER TABLE " + tableName + " ADD " + columnBanned + " boolean NOT NULL DEFAULT 0",
                "ALTER TABLE " + tableName + " ADD " + columnName + " varchar(16) NOT NULL DEFAULT \'Unknown\'",
                "ALTER TABLE " + tableName + " ADD " + columnRegistered + " bigint NOT NULL DEFAULT 0"
            };
        } else {
            queries = new String[]{
                "ALTER TABLE " + tableName + " ADD COLUMN " + columnContainsBukkitData + " boolean NOT NULL DEFAULT 0",
                "ALTER TABLE " + tableName + " ADD COLUMN " + columnOP + " boolean NOT NULL DEFAULT 0",
                "ALTER TABLE " + tableName + " ADD COLUMN " + columnBanned + " boolean NOT NULL DEFAULT 0",
                "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " varchar(16) NOT NULL DEFAULT \'Unknown\'",
                "ALTER TABLE " + tableName + " ADD COLUMN " + columnRegistered + " bigint NOT NULL DEFAULT 0"
            };
        }
        for (String query : queries) {
            try {
                execute(query);
            } catch (Exception e) {
            }
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

    /**
     *
     * @param uuid
     * @return
     * @throws SQLException
     */
    public int getUserId(UUID uuid) throws SQLException {
        return getUserId(uuid.toString());
    }

    /**
     *
     * @param uuid
     * @return
     * @throws SQLException
     */
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

    /**
     *
     * @param userID
     * @return
     * @throws SQLException
     */
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

    /**
     *
     * @return @throws SQLException
     */
    public Set<UUID> getSavedUUIDs() throws SQLException {
        Benchmark.start("Get Saved UUIDS");
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
            Benchmark.stop("Get Saved UUIDS");
        }
    }

    /**
     *
     * @param uuid
     * @return
     */
    public boolean removeUser(UUID uuid) {
        return removeUser(uuid.toString());
    }

    /**
     *
     * @param uuid
     * @return
     */
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

    /**
     *
     * @param uuid
     * @return
     * @throws SQLException
     */
    public UserData getUserData(UUID uuid) throws SQLException {
        Benchmark.start(uuid + " Get UserData");
        boolean containsBukkitData = getContainsBukkitData(uuid);
        UserData data = null;
        if (containsBukkitData) {
            data = getUserDataForKnown(uuid);
        }
        if (data == null) {
            data = new UserData(getOfflinePlayer(uuid), new DemographicsData());
            addUserInformationToUserData(data);
        }
        Benchmark.stop(uuid + " Get UserData");
        return data;
    }

    private boolean getContainsBukkitData(UUID uuid) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        boolean containsBukkitData = false;
        try {
            statement = prepareStatement("SELECT " + columnContainsBukkitData + " FROM " + tableName + " WHERE (" + columnUUID + "=?)");
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            while (set.next()) {
                containsBukkitData = set.getBoolean(columnContainsBukkitData);
            }
        } finally {
            close(statement);
            close(set);
        }
        return containsBukkitData;
    }

    /**
     *
     * @param uuids
     * @return
     * @throws SQLException
     */
    public List<UserData> getUserData(Collection<UUID> uuids) throws SQLException {
        Benchmark.start("Get UserData Multiple " + uuids.size());
        List<UUID> containsBukkitData = getContainsBukkitData(uuids);
        List<UserData> datas = new ArrayList<>();
        datas.addAll(getUserDataForKnown(containsBukkitData));

        uuids.removeAll(containsBukkitData);
        if (!uuids.isEmpty()) {
            List<UserData> noBukkitData = new ArrayList<>();
            Benchmark.start("Create UserData objects for No BukkitData players " + uuids.size());
            for (UUID uuid : uuids) {
                UserData uData = new UserData(getOfflinePlayer(uuid), new DemographicsData());
                noBukkitData.add(uData);
            }
            Benchmark.stop("Create UserData objects for No BukkitData players " + uuids.size());
            addUserInformationToUserData(noBukkitData);
            datas.addAll(noBukkitData);
        }

        Benchmark.stop("Get UserData Multiple " + uuids.size());
        return datas;
    }

    /**
     *
     * @param uuids
     * @return
     * @throws SQLException
     */
    public List<UUID> getContainsBukkitData(Collection<UUID> uuids) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        List<UUID> containsBukkitData = new ArrayList<>();
        try {
            statement = prepareStatement("SELECT " + columnContainsBukkitData + ", " + columnUUID + " FROM " + tableName);
            set = statement.executeQuery();
            while (set.next()) {
                String uuidS = set.getString(columnUUID);
                UUID uuid = UUID.fromString(uuidS);
                if (!uuids.contains(uuid)) {
                    continue;
                }
                boolean contains = set.getBoolean(columnContainsBukkitData);
                if (contains) {
                    containsBukkitData.add(uuid);
                }
            }
        } finally {
            close(statement);
            close(set);
        }
        return containsBukkitData;
    }

    private UserData getUserDataForKnown(UUID uuid) throws SQLException {
        Benchmark.start("getUserDataForKnown UserData");
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE (" + columnUUID + "=?)");
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            while (set.next()) {
                DemographicsData demData = new DemographicsData();
                demData.setAge(set.getInt(columnDemAge));
                demData.setGender(Gender.parse(set.getString(columnDemGender)));
                demData.setGeoLocation(set.getString(columnDemGeoLocation));
                GameMode gm = GameMode.valueOf(set.getString(columnLastGM));
                boolean op = set.getBoolean(columnOP);
                boolean banned = set.getBoolean(columnBanned);
                String name = set.getString(columnName);
                long registered = set.getLong(columnRegistered);
                UserData data = new UserData(uuid, registered, null, op, gm, demData, name, false);
                data.setBanned(banned);
                data.setLastGamemode(gm);
                data.setLastGmSwapTime(set.getLong(columnLastGMSwapTime));
                data.setPlayTime(set.getLong(columnPlayTime));
                data.setLoginTimes(set.getInt(columnLoginTimes));
                data.setLastPlayed(set.getLong(columnLastPlayed));
                data.setDeaths(set.getInt(columnDeaths));
                data.setMobKills(set.getInt(columnMobKills));
                return data;
            }
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("getUserDataForKnown UserData");
        }
        return null;
    }

    private List<UserData> getUserDataForKnown(Collection<UUID> uuids) throws SQLException {
        Benchmark.start("getUserDataForKnown Multiple " + uuids.size());
        PreparedStatement statement = null;
        ResultSet set = null;
        List<UserData> datas = new ArrayList<>();
        try {
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            while (set.next()) {
                String uuidS = set.getString(columnUUID);
                UUID uuid = UUID.fromString(uuidS);
                if (!uuids.contains(uuid)) {
                    continue;
                }
                DemographicsData demData = new DemographicsData();
                demData.setAge(set.getInt(columnDemAge));
                demData.setGender(Gender.parse(set.getString(columnDemGender)));
                demData.setGeoLocation(set.getString(columnDemGeoLocation));
                GameMode gm = GameMode.valueOf(set.getString(columnLastGM));
                boolean op = set.getBoolean(columnOP);
                boolean banned = set.getBoolean(columnBanned);
                String name = set.getString(columnName);
                long registered = set.getLong(columnRegistered);
                UserData data = new UserData(uuid, registered, null, op, gm, demData, name, false);
                data.setBanned(banned);
                data.setLastGamemode(gm);
                data.setLastGmSwapTime(set.getLong(columnLastGMSwapTime));
                data.setPlayTime(set.getLong(columnPlayTime));
                data.setLoginTimes(set.getInt(columnLoginTimes));
                data.setLastPlayed(set.getLong(columnLastPlayed));
                data.setDeaths(set.getInt(columnDeaths));
                data.setMobKills(set.getInt(columnMobKills));
                datas.add(data);
            }
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("getUserDataForKnown Multiple " + uuids.size());
        }
        return datas;
    }

    /**
     *
     * @param data
     * @throws SQLException
     */
    public void addUserInformationToUserData(UserData data) throws SQLException {
        Benchmark.start("addUserInformationToUserData UserData");
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
            Benchmark.stop("addUserInformationToUserData UserData");
        }
    }

    /**
     *
     * @param data
     * @throws SQLException
     */
    public void addUserInformationToUserData(List<UserData> data) throws SQLException {
        Benchmark.start("addUserInformationToUserData Multiple " + data.size());
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            Map<UUID, UserData> userDatas = data.stream().collect(Collectors.toMap(UserData::getUuid, Function.identity()));
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            while (set.next()) {
                String uuidS = set.getString(columnUUID);
                UUID uuid = UUID.fromString(uuidS);
                if (!userDatas.keySet().contains(uuid)) {
                    continue;
                }
                UserData uData = userDatas.get(uuid);
                uData.getDemData().setAge(set.getInt(columnDemAge));
                uData.getDemData().setGender(Gender.parse(set.getString(columnDemGender)));
                uData.getDemData().setGeoLocation(set.getString(columnDemGeoLocation));
                uData.setLastGamemode(GameMode.valueOf(set.getString(columnLastGM)));
                uData.setLastGmSwapTime(set.getLong(columnLastGMSwapTime));
                uData.setPlayTime(set.getLong(columnPlayTime));
                uData.setLoginTimes(set.getInt(columnLoginTimes));
                uData.setLastPlayed(set.getLong(columnLastPlayed));
                uData.setDeaths(set.getInt(columnDeaths));
                uData.setMobKills(set.getInt(columnMobKills));
            }
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("addUserInformationToUserData Multiple " + data.size());
        }
    }

    /**
     *
     * @param data
     * @throws SQLException
     */
    public void saveUserDataInformation(UserData data) throws SQLException {
        Benchmark.start("Save UserInfo");
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
                        + columnMobKills + "=?, "
                        + columnContainsBukkitData + "=?, "
                        + columnOP + "=?, "
                        + columnBanned + "=?, "
                        + columnName + "=?, "
                        + columnRegistered + "=? "
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
                statement.setBoolean(11, data.getName() != null);
                statement.setBoolean(12, data.isOp());
                statement.setBoolean(13, data.isBanned());
                statement.setString(14, data.getName());
                statement.setLong(15, data.getRegistered());
                statement.setString(16, uuid.toString());
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
                        + columnMobKills + ", "
                        + columnContainsBukkitData + ", "
                        + columnOP + ", "
                        + columnBanned + ", "
                        + columnName + ", "
                        + columnRegistered
                        + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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
                statement.setBoolean(12, data.getName() != null);
                statement.setBoolean(13, data.isOp());
                statement.setBoolean(14, data.isBanned());
                statement.setString(15, data.getName());
                statement.setLong(16, data.getRegistered());
                statement.execute();
            }
        } finally {
            close(statement);
            Benchmark.stop("Save UserInfo");
        }
    }

    /**
     *
     * @param data
     * @throws SQLException
     */
    public void saveUserDataInformationBatch(Collection<UserData> data) throws SQLException {
        Benchmark.start("Save UserInfo multiple " + data.size());
        try {
            List<UserData> newUserdata = updateExistingUserData(data);
            Benchmark.start("Insert new UserInfo multiple " + newUserdata.size());
            insertNewUserData(newUserdata);
            Benchmark.stop("Insert new UserInfo multiple " + newUserdata.size());
        } finally {
            Benchmark.stop("Save UserInfo multiple " + data.size());
        }
    }

    private void insertNewUserData(Collection<UserData> data) throws SQLException {
        PreparedStatement statement = null;
        try {
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
                    + columnMobKills + ", "
                    + columnContainsBukkitData + ", "
                    + columnOP + ", "
                    + columnBanned + ", "
                    + columnName + ", "
                    + columnRegistered
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            boolean commitRequired = false;
            int i = 0;
            for (UserData uData : data) {
                UUID uuid = uData.getUuid();
                statement.setString(1, uuid.toString());
                statement.setInt(2, uData.getDemData().getAge());
                statement.setString(3, uData.getDemData().getGender().toString().toLowerCase());
                statement.setString(4, uData.getDemData().getGeoLocation());
                GameMode gm = uData.getLastGamemode();
                if (gm != null) {
                    statement.setString(5, uData.getLastGamemode().name());
                } else {
                    statement.setString(5, GameMode.SURVIVAL.name());
                }
                statement.setLong(6, uData.getLastGmSwapTime());
                statement.setLong(7, uData.getPlayTime());
                statement.setInt(8, uData.getLoginTimes());
                statement.setLong(9, uData.getLastPlayed());
                statement.setInt(10, uData.getDeaths());
                statement.setInt(11, uData.getMobKills());
                statement.setBoolean(12, uData.getName() != null);
                statement.setBoolean(13, uData.isOp());
                statement.setBoolean(14, uData.isBanned());
                statement.setString(15, uData.getName());
                statement.setLong(16, uData.getRegistered());
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

    private List<UserData> updateExistingUserData(Collection<UserData> data) throws SQLException {
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
                    + columnMobKills + "=?, "
                    + columnContainsBukkitData + "=?, "
                    + columnOP + "=?, "
                    + columnBanned + "=?, "
                    + columnName + "=?, "
                    + columnRegistered + "=? "
                    + "WHERE " + columnUUID + "=?";
            statement = prepareStatement(uSQL);
            boolean commitRequired = false;
            Set<UUID> savedUUIDs = getSavedUUIDs();
            int i = 0;
            for (UserData uData : data) {
                if (uData == null) {
                    continue;
                }
                UUID uuid = uData.getUuid();
                if (uuid == null) {
                    try {
                        uData.setUuid(UUIDUtility.getUUIDOf(uData.getName(), db));
                    } catch (Exception ex) {
                        continue;
                    }
                }
                uuid = uData.getUuid();
                if (uuid == null) {
                    continue;
                }
                if (!savedUUIDs.contains(uuid)) {
                    if (!saveLast.contains(uData)) {
                        saveLast.add(uData);
                    }
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
                statement.setBoolean(11, uData.getName() != null);
                statement.setBoolean(12, uData.isOp());
                statement.setBoolean(13, uData.isBanned());
                statement.setString(14, uData.getName());
                statement.setLong(15, uData.getRegistered());
                statement.setString(16, uuid.toString());
                statement.addBatch();
                uData.stopAccessing();
                commitRequired = true;
                i++;
            }
            if (commitRequired) {
                Log.debug("Executing userinfo batch update: " + i);
                statement.executeBatch();
            }
            return saveLast;
        } finally {
            close(statement);
        }
    }

    /**
     *
     * @param uuids
     * @return
     * @throws SQLException
     */
    public Map<UUID, Integer> getUserIds(Collection<UUID> uuids) throws SQLException {
        Benchmark.start("Get User IDS " + uuids.size());
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            Map<UUID, Integer> ids = new HashMap<>();
            statement = prepareStatement("SELECT " + columnUUID + ", " + columnID + " FROM " + tableName);
            set = statement.executeQuery();
            while (set.next()) {
                String uuidS = set.getString(columnUUID);
                UUID uuid = UUID.fromString(uuidS);
                if (!uuids.contains(uuid)) {
                    continue;
                }
                ids.put(uuid, set.getInt(columnID));
            }
            return ids;
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("Get User IDS " + uuids.size());
        }
    }

    /**
     *
     * @return @throws SQLException
     */
    public Map<UUID, Integer> getAllUserIds() throws SQLException {
        Benchmark.start("Get User IDS ALL");
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            Map<UUID, Integer> ids = new HashMap<>();
            statement = prepareStatement("SELECT " + columnUUID + ", " + columnID + " FROM " + tableName);
            set = statement.executeQuery();
            while (set.next()) {
                String uuidS = set.getString(columnUUID);
                UUID uuid = UUID.fromString(uuidS);
                ids.put(uuid, set.getInt(columnID));
            }
            return ids;
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("Get User IDS ALL");
        }
    }

    public Map<Integer, Integer> getLoginTimes() throws SQLException {
        Benchmark.start("Get Logintimes");
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            Map<Integer, Integer> ids = new HashMap<>();
            statement = prepareStatement("SELECT " + columnID + ", " + columnLoginTimes + " FROM " + tableName);
            set = statement.executeQuery();
            while (set.next()) {
                Integer id = set.getInt(columnID);
                ids.put(id, set.getInt(columnLoginTimes));
            }
            return ids;
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("Get Logintimes");
        }
    }

    /**
     *
     * @return
     */
    public String getColumnID() {
        return columnID;
    }

    public UUID getUuidOf(String playername) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT " + columnUUID + " FROM " + tableName + " WHERE (UPPER(" + columnName + ")=UPPER(?))");
            statement.setString(1, playername);
            set = statement.executeQuery();
            while (set.next()) {
                String uuidS = set.getString(columnUUID);
                UUID uuid = UUID.fromString(uuidS);
                return uuid;
            }
            return null;
        } finally {
            close(set);
            close(statement);
        }
    }

    public Map<Integer, Long> getLoginTimes(Collection<UUID> uuids) {
        //TODO
        return new HashMap<>();
    }
}
