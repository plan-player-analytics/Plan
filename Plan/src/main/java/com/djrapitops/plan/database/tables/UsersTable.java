package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Select;
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
public class UsersTable extends Table {

    private final String columnID = "id";
    private final String columnUUID = "uuid";
    @Deprecated
    private final String columnGeolocation;
    @Deprecated
    private final String columnLastGM;
    @Deprecated
    private final String columnLastGMSwapTime;
    @Deprecated
    private final String columnPlayTime;
    @Deprecated
    private final String columnLoginTimes;
    @Deprecated
    private final String columnLastPlayed;
    @Deprecated
    private final String columnDeaths;
    @Deprecated
    private final String columnMobKills;
    private final String columnRegistered;
    private final String columnName;
    //TODO Server Specific Table (Also has registered on it)
    @Deprecated
    private final String columnOP;
    @Deprecated
    private final String columnBanned;
    //
    @Deprecated
    private final String columnContainsBukkitData;
    @Deprecated
    private final String columnLastWorldSwapTime;
    @Deprecated
    private final String columnLastWorld;

    public final String statementSelectID;

    /**
     * @param db
     * @param usingMySQL
     */
    public UsersTable(SQLDB db, boolean usingMySQL) {
        super("plan_users", db, usingMySQL);
        statementSelectID = "(" + Select.from(tableName, tableName + "." + columnID).where(columnUUID + "=?").toString() + ")";

        columnGeolocation = "geolocation";
        columnLastGM = "last_gamemode";
        columnLastGMSwapTime = "last_gamemode_swap";
        columnPlayTime = "play_time";
        columnLoginTimes = "login_times";
        columnLastPlayed = "last_played";
        columnMobKills = "mob_kills";

        columnDeaths = "deaths";
        columnRegistered = "registered";
        columnOP = "opped";
        columnName = "name";
        columnBanned = "banned";
        columnContainsBukkitData = "contains_bukkit_data";

        columnLastWorldSwapTime = "last_world_swap";
        columnLastWorld = "last_world";
    }

    /**
     * @return
     */
    @Override
    public boolean createTable() {
        try {
            execute(TableSqlParser.createTable(tableName)
                    .primaryKeyIDColumn(usingMySQL, columnID, Sql.INT)
                    .column(columnUUID, Sql.varchar(36)).notNull().unique()
                    .column(columnGeolocation, Sql.varchar(50)).notNull()
                    .column(columnLastGM, Sql.varchar(15)).notNull()
                    .column(columnLastGMSwapTime, Sql.LONG).notNull()
                    .column(columnPlayTime, Sql.LONG).notNull()
                    .column(columnLoginTimes, Sql.INT).notNull()
                    .column(columnLastPlayed, Sql.LONG).notNull()
                    .column(columnDeaths, Sql.INT).notNull()
                    .column(columnMobKills, Sql.INT).notNull()
                    .column(columnRegistered, Sql.LONG).notNull()
                    .column(columnOP, Sql.BOOL).notNull().defaultValue(false)
                    .column(columnName, Sql.varchar(16)).notNull()
                    .column(columnBanned, Sql.BOOL).notNull().defaultValue(false)
                    .column(columnContainsBukkitData, Sql.BOOL).notNull().defaultValue(false)
                    .column(columnLastWorld, Sql.varchar(255)).notNull()
                    .column(columnLastWorldSwapTime, Sql.LONG).notNull()
                    .primaryKey(usingMySQL, columnID)
                    .toString()
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
            if (version < 8) {
                alterTablesV8();
            }
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    private void alterTablesV8() {
        addColumns(
                columnLastWorldSwapTime + " bigint NOT NULL DEFAULT 0",
                columnLastWorld + " varchar(255) NOT NULL DEFAULT 'Unknown'"
        );
    }

    private void alterTablesV5() {
        removeColumns("age", "gender");
    }

    private void alterTablesV4() {
        addColumns(
                columnContainsBukkitData + " boolean NOT NULL DEFAULT 0",
                columnOP + " boolean NOT NULL DEFAULT 0",
                columnBanned + " boolean NOT NULL DEFAULT 0",
                columnName + " varchar(16) NOT NULL DEFAULT 'Unknown'",
                columnRegistered + " bigint NOT NULL DEFAULT 0"
        );
    }

    private void alterTablesV3() {
        addColumns(
                columnDeaths + " integer NOT NULL DEFAULT 0",
                columnMobKills + " integer NOT NULL DEFAULT 0"
        );
        removeColumns("player_kills");
    }

    /**
     * @param uuid
     * @return
     * @throws SQLException
     */
    public int getUserId(UUID uuid) throws SQLException {
        return getUserId(uuid.toString());
    }

    /**
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
                uuids.add(uuid);
            }
            return uuids;
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("Database", "Get Saved UUIDS");
        }
    }

    /**
     * @param uuid
     * @return
     */
    public boolean removeUser(UUID uuid) {
        return removeUser(uuid.toString());
    }

    /**
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

    /**
     * @param uuids
     * @return
     * @throws SQLException
     */
    public Map<UUID, Integer> getUserIds(Collection<UUID> uuids) throws SQLException {
        Benchmark.start("Get User IDS Multiple");
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
            Benchmark.stop("Database", "Get User IDS Multiple");
        }
    }

    /**
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
            Benchmark.stop("Database", "Get User IDS ALL");
        }
    }

    /**
     * @return
     */
    public String getColumnID() {
        return columnID;
    }

    /**
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
            if (set.next()) {
                String uuidS = set.getString(columnUUID);
                return UUID.fromString(uuidS);
            }
            return null;
        } finally {
            close(set);
            close(statement);
        }
    }

    public List<UserData> getUserData(List<UUID> uuids) {
        // TODO Rewrite method for new UserData objects.
        return new ArrayList<>();
    }

    public String getColumnUUID() {
        return columnUUID;
    }
}
