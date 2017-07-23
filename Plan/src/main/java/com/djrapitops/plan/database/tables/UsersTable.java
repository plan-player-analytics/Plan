package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.player.Fetch;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.database.DBUtils;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;
import org.bukkit.GameMode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Rsl1122
 */
public class UsersTable extends Table {

    private final String columnID;
    private final String columnUUID;
    @Deprecated // Removed in 3.5.2
    private final String columnDemAge;
    @Deprecated // Removed in 3.5.2
    private final String columnDemGender;
    private final String columnGeolocation;
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
        columnGeolocation = "geolocation";
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
                    + columnGeolocation + " varchar(50) NOT NULL, "
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
            if (version < 5) {
                alterTablesV5();
            }
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    private void alterTablesV5() {
        if (usingMySQL) {
            try {
                execute("ALTER TABLE " + tableName
                        + " DROP COLUMN " + columnDemAge + ","
                        + " DROP COLUMN " + columnDemGender);
            } catch (Exception e) {
            }
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
        Benchmark.start("Database: Get Saved UUIDS");
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
            Benchmark.stop("Database: Get Saved UUIDS");
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
        Benchmark.start("Database: Get UserData");
        boolean containsBukkitData = getContainsBukkitData(uuid);
        UserData data = null;
        if (containsBukkitData) {
            data = getUserDataForKnown(uuid);
        }
        if (data == null) {
            data = new UserData(Fetch.getIOfflinePlayer(uuid));
            addUserInformationToUserData(data);
        }
        Benchmark.stop("Database: Get UserData");
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
        Benchmark.start("Database: Get UserData Multiple");
        List<UUID> containsBukkitData = getContainsBukkitData(uuids);
        List<UserData> datas = new ArrayList<>();
        datas.addAll(getUserDataForKnown(containsBukkitData));

        uuids.removeAll(containsBukkitData);
        if (!uuids.isEmpty()) {
            List<UserData> noBukkitData = new ArrayList<>();
            Benchmark.start("Database: Create UserData objects for No BukkitData players");
            for (UUID uuid : uuids) {
                UserData uData = new UserData(Fetch.getIOfflinePlayer(uuid));
                noBukkitData.add(uData);
            }
            Benchmark.stop("Database: Create UserData objects for No BukkitData players");
            addUserInformationToUserData(noBukkitData);
            datas.addAll(noBukkitData);
        }

        Benchmark.stop("Database: Get UserData Multiple");
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
        Benchmark.start("Database: getUserDataForKnown UserData");
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE (" + columnUUID + "=?)");
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            while (set.next()) {

                String gm = set.getString(columnLastGM);
                boolean op = set.getBoolean(columnOP);
                boolean banned = set.getBoolean(columnBanned);
                String name = set.getString(columnName);
                long registered = set.getLong(columnRegistered);
                UserData data = new UserData(uuid, registered, op, gm, name, false);
                data.setBanned(banned);
                data.setLastGamemode(gm);
                data.setGeolocation(set.getString(columnGeolocation));
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
            Benchmark.stop("Database: getUserDataForKnown UserData");
        }
        return null;
    }

    private List<UserData> getUserDataForKnown(Collection<UUID> uuids) throws SQLException {
        Benchmark.start("Database: getUserDataForKnown Multiple");
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
                String gm = set.getString(columnLastGM);
                boolean op = set.getBoolean(columnOP);
                boolean banned = set.getBoolean(columnBanned);
                String name = set.getString(columnName);
                long registered = set.getLong(columnRegistered);
                UserData data = new UserData(uuid, registered, op, gm, name, false);
                data.setBanned(banned);
                data.setLastGamemode(gm);
                data.setGeolocation(set.getString(columnGeolocation));
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
            Benchmark.stop("Database: getUserDataForKnown Multiple");
        }
        return datas;
    }

    /**
     *
     * @param data
     * @throws SQLException
     */
    public void addUserInformationToUserData(UserData data) throws SQLException {
        Benchmark.start("Database: addUserInformationToUserData");
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE (" + columnUUID + "=?)");
            statement.setString(1, data.getUuid().toString());
            set = statement.executeQuery();
            while (set.next()) {
                data.setGeolocation(set.getString(columnGeolocation));
                data.setLastGamemode(set.getString(columnLastGM));
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
            Benchmark.stop("Database: addUserInformationToUserData");
        }
    }

    /**
     *
     * @param data
     * @throws SQLException
     */
    public void addUserInformationToUserData(List<UserData> data) throws SQLException {
        Benchmark.start("Database: addUserInformationToUserData Multiple");
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
                uData.setGeolocation(set.getString(columnGeolocation));
                uData.setLastGamemode(set.getString(columnLastGM));
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
            Benchmark.stop("Database: addUserInformationToUserData Multiple");
        }
    }

    /**
     *
     * @param data
     * @throws SQLException
     */
    public void saveUserDataInformation(UserData data) throws SQLException {
        Benchmark.start("Database: Save UserInfo");
        PreparedStatement statement = null;
        try {
            UUID uuid = data.getUuid();
            int userId = getUserId(uuid);
            int update = 0;
            if (userId != -1) {
                String sql = getUpdateStatement();

                statement = prepareStatement(sql);
                statement.setString(1, data.getGeolocation());
                String gm = data.getLastGamemode();
                if (gm != null) {
                    statement.setString(2, gm);
                } else {
                    statement.setString(2, "SURVIVAL");
                }
                statement.setLong(3, data.getLastGmSwapTime());
                statement.setLong(4, data.getPlayTime());
                statement.setInt(5, data.getLoginTimes());
                statement.setLong(6, data.getLastPlayed());
                statement.setInt(7, data.getDeaths());
                statement.setInt(8, data.getMobKills());
                statement.setBoolean(9, data.getName() != null);
                statement.setBoolean(10, data.isOp());
                statement.setBoolean(11, data.isBanned());
                statement.setString(12, data.getName());
                statement.setLong(13, data.getRegistered());
                statement.setString(14, uuid.toString());
                update = statement.executeUpdate();
            }
            if (update == 0) {
                close(statement);
                statement = prepareStatement(getInsertStatement());

                statement.setString(1, uuid.toString());
                statement.setString(2, data.getGeolocation());
                String gm = data.getLastGamemode();
                if (gm != null) {
                    statement.setString(3, gm);
                } else {
                    statement.setString(3, "SURVIVAL");
                }
                statement.setLong(4, data.getLastGmSwapTime());
                statement.setLong(5, data.getPlayTime());
                statement.setInt(6, data.getLoginTimes());
                statement.setLong(7, data.getLastPlayed());
                statement.setInt(8, data.getDeaths());
                statement.setInt(9, data.getMobKills());
                statement.setBoolean(10, data.getName() != null);
                statement.setBoolean(11, data.isOp());
                statement.setBoolean(12, data.isBanned());
                statement.setString(13, data.getName());
                statement.setLong(14, data.getRegistered());
                statement.execute();
            }
        } finally {
            close(statement);
            Benchmark.stop("Database: Save UserInfo");
        }
    }

    private boolean tableHasV4Columns() {
        if (usingMySQL) {
            return false;
        } else {
            PreparedStatement statement = null;
            ResultSet set = null;
            try {
                try {
                    statement = prepareStatement("SELECT " + columnDemAge + " FROM " + tableName + " LIMIT 1");
                    set = statement.executeQuery();
                    Log.debug("UsersTable has V4 columns.");
                    return true;
                } catch (SQLException e) {
                    return false;
                }
            } finally {
                close(set, statement);
            }
        }
    }

    private String getInsertStatement() {
        final boolean hasV4Columns = tableHasV4Columns();
        String v4rows = hasV4Columns ? columnDemAge + ", " + columnDemGender + ", " : "";
        String v4values = hasV4Columns ? "-1, Deprecated, " : "";
        return "INSERT INTO " + tableName + " ("
                + v4rows
                + columnUUID + ", "
                + columnGeolocation + ", "
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
                + ") VALUES (" + v4values + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private String getUpdateStatement() {
        final boolean hasV4Columns = tableHasV4Columns();
        String v4rows = hasV4Columns ? columnDemAge + "=-1, " + columnDemGender + "='Deprecated', " : "";
        String sql = "UPDATE " + tableName + " SET "
                + v4rows
                + columnGeolocation + "=?, "
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
        return sql;
    }

    /**
     *
     * @param data
     * @throws SQLException
     */
    public void saveUserDataInformationBatch(Collection<UserData> data) throws SQLException {
        Benchmark.start("Database: Save UserInfo multiple");
        try {
            List<UserData> newUserdata = updateExistingUserData(data);
            Benchmark.start("Database: Insert new UserInfo multiple");
            List<List<UserData>> batches = DBUtils.splitIntoBatches(newUserdata);
            for (List<UserData> batch : batches) {
                insertNewUserData(batch);
            }
            Benchmark.stop("Database: Insert new UserInfo multiple");
        } finally {
            Benchmark.stop("Database: Save UserInfo multiple");
        }
    }

    private void insertNewUserData(Collection<UserData> data) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(getInsertStatement());
            boolean commitRequired = false;
            int i = 0;
            for (UserData uData : data) {
                UUID uuid = uData.getUuid();
                statement.setString(1, uuid.toString());
                statement.setString(2, uData.getGeolocation());
                String gm = uData.getLastGamemode();
                if (gm != null) {
                    statement.setString(3, gm);
                } else {
                    statement.setString(3, "SURVIVAL");
                }
                statement.setLong(4, uData.getLastGmSwapTime());
                statement.setLong(5, uData.getPlayTime());
                statement.setInt(6, uData.getLoginTimes());
                statement.setLong(7, uData.getLastPlayed());
                statement.setInt(8, uData.getDeaths());
                statement.setInt(9, uData.getMobKills());
                statement.setBoolean(10, uData.getName() != null);
                statement.setBoolean(11, uData.isOp());
                statement.setBoolean(12, uData.isBanned());
                statement.setString(13, uData.getName());
                statement.setLong(14, uData.getRegistered());
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
            String sql = getUpdateStatement();
            statement = prepareStatement(sql);
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
                statement.setString(1, uData.getGeolocation());
                String gm = uData.getLastGamemode();
                if (gm != null) {
                    statement.setString(2, gm);
                } else {
                    statement.setString(2, GameMode.SURVIVAL.name());
                }
                statement.setLong(3, uData.getLastGmSwapTime());
                statement.setLong(4, uData.getPlayTime());
                statement.setInt(5, uData.getLoginTimes());
                statement.setLong(6, uData.getLastPlayed());
                statement.setInt(7, uData.getDeaths());
                statement.setInt(8, uData.getMobKills());
                statement.setBoolean(9, uData.getName() != null);
                statement.setBoolean(10, uData.isOp());
                statement.setBoolean(11, uData.isBanned());
                statement.setString(12, uData.getName());
                statement.setLong(13, uData.getRegistered());
                statement.setString(14, uuid.toString());
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
        Benchmark.start("Database: Get User IDS Multiple");
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
            Benchmark.stop("Database: Get User IDS Multiple");
        }
    }

    /**
     *
     * @return @throws SQLException
     */
    public Map<UUID, Integer> getAllUserIds() throws SQLException {
        Benchmark.start("Database: Get User IDS ALL");
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
            Benchmark.stop("Database: Get User IDS ALL");
        }
    }

    /**
     *
     * @return @throws SQLException
     */
    public Map<Integer, Integer> getLoginTimes() throws SQLException {
        Benchmark.start("Database: Get Logintimes");
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
            Benchmark.stop("Database: Get Logintimes");
        }
    }

    /**
     *
     * @return
     */
    public String getColumnID() {
        return columnID;
    }

    /**
     *
     * @param playername
     * @return
     * @throws SQLException
     */
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

    /**
     *
     * @param uuids
     * @return
     */
    public Map<Integer, Long> getLoginTimes(Collection<UUID> uuids) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }
}
