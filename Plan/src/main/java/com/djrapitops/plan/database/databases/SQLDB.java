package main.java.com.djrapitops.plan.database.databases;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.Gender;
import main.java.com.djrapitops.plan.data.*;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.database.Database;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import static org.bukkit.Bukkit.getOfflinePlayer;

public abstract class SQLDB extends Database {

    final Plan plugin;

    private final boolean supportsModification;

    private Connection connection;

    private final String userName;
    private final String locationName;
    private final String serverdataName;
    private final String commanduseName;
    private final String gamemodetimesName;
    private final String nicknamesName;
    private final String ipsName;
    private final String sessionName;
    private final String killsName;

    private final String userColumnUUID;
    private final String userColumnID;
    private final String userColumnPlayTime;
    private final String userColumnDemGeoLocation;
    private final String userColumnDemAge;
    private final String userColumnDemGender;
    private final String userColumnLastGM;
    private final String userColumnLastGMSwapTime;
    private final String userColumnLoginTimes;
    private final String userColumnLastPlayed;
    private final String userColumnMobKills;
    private final String userColumnPlayerKills;
    private final String userColumnDeaths;

    private final String locationColumnUserID;
    private final String locationColumnID;
    private final String locationColumnCoordinatesX;
    private final String locationColumnCoordinatesZ;
    private final String locationColumnWorld;

    private final String commanduseColumnCommand;
    private final String commanduseColumnTimesUsed;

    private final String gamemodetimesColumnUserID;
    private final String gamemodetimesColumnSurvivalTime;
    private final String gamemodetimesColumnCreativeTime;
    private final String gamemodetimesColumnAdventureTime;
    private final String gamemodetimesColumnSpectatorTime;

    private final String nicknamesColumnUserID;
    private final String nicknamesColumnNick;
    private final String nicknamesColumnCurrent;
    private final String ipsColumnUserID;
    private final String ipsColumnIP;

    private final String sessionColumnUserID;
    private final String sessionColumnSessionStart;
    private final String sessionColumnSessionEnd;

    private final String killsColumnKillerUserID;
    private final String killsColumnVictimUserID;
    private final String killsColumnWeapon;
    private final String killsColumnDate;

    private String versionName;

    public SQLDB(Plan plugin, boolean supportsModification) {
        super(plugin);
        this.plugin = plugin;
        this.supportsModification = supportsModification;

        userName = "plan_users";
        locationName = "plan_locations";
        nicknamesName = "plan_nicknames";
        commanduseName = "plan_commandusages";
        gamemodetimesName = "plan_gamemodetimes";
        serverdataName = "plan_serverdata";
        ipsName = "plan_ips";
        sessionName = "plan_sessions";
        killsName = "plan_kills";

        userColumnID = "id";
        locationColumnID = "id";
        userColumnUUID = "uuid";
        locationColumnUserID = "user_id";
        nicknamesColumnUserID = "user_id";
        gamemodetimesColumnUserID = "user_id";
        ipsColumnUserID = "user_id";
        sessionColumnUserID = "user_id";
        killsColumnKillerUserID = "killer_id";
        killsColumnVictimUserID = "victim_id";

        userColumnDemAge = "age";
        userColumnDemGender = "gender";
        userColumnDemGeoLocation = "geolocation";
        userColumnLastGM = "last_gamemode";
        userColumnLastGMSwapTime = "last_gamemode_swap";
        userColumnPlayTime = "play_time";
        userColumnLoginTimes = "login_times";
        userColumnLastPlayed = "last_played";
        userColumnMobKills = "mob_kills";
        userColumnPlayerKills = "player_kills";
        userColumnDeaths = "deaths";

        locationColumnCoordinatesX = "x";
        locationColumnCoordinatesZ = "z";
        locationColumnWorld = "world_name";

        nicknamesColumnNick = "nickname";
        nicknamesColumnCurrent = "current_nick";

        gamemodetimesColumnSurvivalTime = "survival";
        gamemodetimesColumnCreativeTime = "creative";
        gamemodetimesColumnAdventureTime = "adventure";
        gamemodetimesColumnSpectatorTime = "spectator";

        ipsColumnIP = "ip";

        commanduseColumnCommand = "command";
        commanduseColumnTimesUsed = "times_used";

        sessionColumnSessionStart = "session_start";
        sessionColumnSessionEnd = "session_end";

        killsColumnWeapon = "weapon";
        killsColumnDate = "date";

        versionName = "plan_version";

        // Maintains Connection.
        (new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.createStatement().execute("/* ping */ SELECT 1");
                    }
                } catch (SQLException e) {
                    connection = getNewConnection();
                }
            }
        }).runTaskTimerAsynchronously(plugin, 60 * 20, 60 * 20);
    }

    @Override
    public boolean init() {
        super.init();
        try {
            return checkConnection();
        } catch (SQLException e) {
            plugin.toLog(this.getClass().getName(), e);
            return false;
        }
    }

    public boolean checkConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = getNewConnection();

            if (connection == null || connection.isClosed()) {
                return false;
            }
            boolean usingMySQL = supportsModification;

            boolean newDatabase = true;
            try {
                getVersion();
                newDatabase = false;
            } catch (Exception e) {
            }
            query("CREATE TABLE IF NOT EXISTS " + userName + " ("
                    + userColumnID + " integer " + ((usingMySQL) ? "NOT NULL AUTO_INCREMENT" : "PRIMARY KEY") + ", "
                    + userColumnUUID + " varchar(36) NOT NULL UNIQUE, "
                    + userColumnDemAge + " integer NOT NULL, "
                    + userColumnDemGender + " varchar(8) NOT NULL, "
                    + userColumnDemGeoLocation + " varchar(50) NOT NULL, "
                    + userColumnLastGM + " varchar(15) NOT NULL, "
                    + userColumnLastGMSwapTime + " bigint NOT NULL, "
                    + userColumnPlayTime + " bigint NOT NULL, "
                    + userColumnLoginTimes + " integer NOT NULL, "
                    + userColumnLastPlayed + " bigint NOT NULL, "
                    + userColumnDeaths + " int NOT NULL, "
                    + userColumnMobKills + " int NOT NULL"
                    + (usingMySQL ? ", PRIMARY KEY (" + userColumnID + ")" : "")
                    + ")"
            );

            query("CREATE TABLE IF NOT EXISTS " + locationName + " ("
                    + locationColumnID + " integer " + ((usingMySQL) ? "NOT NULL AUTO_INCREMENT" : "PRIMARY KEY") + ", "
                    + locationColumnUserID + " integer NOT NULL, "
                    + locationColumnCoordinatesX + " integer NOT NULL, "
                    + locationColumnCoordinatesZ + " integer NOT NULL, "
                    + locationColumnWorld + " varchar(64) NOT NULL, "
                    + (usingMySQL ? "PRIMARY KEY (" + userColumnID + "), " : "")
                    + "FOREIGN KEY(" + locationColumnUserID + ") REFERENCES " + userName + "(" + userColumnID + ")"
                    + ")"
            );

            query("CREATE TABLE IF NOT EXISTS " + gamemodetimesName + " ("
                    + gamemodetimesColumnUserID + " integer NOT NULL, "
                    + gamemodetimesColumnSurvivalTime + " bigint NOT NULL, "
                    + gamemodetimesColumnCreativeTime + " bigint NOT NULL, "
                    + gamemodetimesColumnAdventureTime + " bigint NOT NULL, "
                    + gamemodetimesColumnSpectatorTime + " bigint NOT NULL, "
                    + "FOREIGN KEY(" + gamemodetimesColumnUserID + ") REFERENCES " + userName + "(" + userColumnID + ")"
                    + ")"
            );

            query("CREATE TABLE IF NOT EXISTS " + ipsName + " ("
                    + ipsColumnUserID + " integer NOT NULL, "
                    + ipsColumnIP + " varchar(20) NOT NULL, "
                    + "FOREIGN KEY(" + ipsColumnUserID + ") REFERENCES " + userName + "(" + userColumnID + ")"
                    + ")"
            );

            query("CREATE TABLE IF NOT EXISTS " + nicknamesName + " ("
                    + nicknamesColumnUserID + " integer NOT NULL, "
                    + nicknamesColumnNick + " varchar(75) NOT NULL, "
                    + nicknamesColumnCurrent + " boolean NOT NULL DEFAULT 0, "
                    + "FOREIGN KEY(" + nicknamesColumnUserID + ") REFERENCES " + userName + "(" + userColumnID + ")"
                    + ")"
            );
            query("CREATE TABLE IF NOT EXISTS " + sessionName + " ("
                    + sessionColumnUserID + " integer NOT NULL, "
                    + sessionColumnSessionStart + " bigint NOT NULL, "
                    + sessionColumnSessionEnd + " bigint NOT NULL, "
                    + "FOREIGN KEY(" + sessionColumnUserID + ") REFERENCES " + userName + "(" + userColumnID + ")"
                    + ")"
            );

            query("CREATE TABLE IF NOT EXISTS " + killsName + " ("
                    + killsColumnKillerUserID + " integer NOT NULL, "
                    + killsColumnVictimUserID + " integer NOT NULL, "
                    + killsColumnWeapon + " varchar(30) NOT NULL, "
                    + killsColumnDate + " bigint NOT NULL, "
                    + "FOREIGN KEY(" + killsColumnKillerUserID + ") REFERENCES " + userName + "(" + userColumnID + "), "
                    + "FOREIGN KEY(" + killsColumnVictimUserID + ") REFERENCES " + userName + "(" + userColumnID + ")"
                    + ")"
            );

            query("CREATE TABLE IF NOT EXISTS " + commanduseName + " ("
                    + commanduseColumnCommand + " varchar(20) NOT NULL, "
                    + commanduseColumnTimesUsed + " integer NOT NULL"
                    + ")"
            );

            query("CREATE TABLE IF NOT EXISTS " + versionName + " ("
                    + "version integer NOT NULL"
                    + ")"
            );
            if (newDatabase) {
                plugin.log("New Database created.");
                setVersion(3);
            }
            int version = getVersion();
            if (version < 3) {
                String sqlite = usingMySQL ? "" : "COLUMN ";
                String[] queries = new String[]{
                    "ALTER TABLE " + userName + " ADD " + sqlite + userColumnDeaths + " integer NOT NULL DEFAULT 0",
                    "ALTER TABLE " + userName + " ADD " + sqlite + userColumnMobKills + " integer NOT NULL DEFAULT 0",
                    "ALTER TABLE " + nicknamesName + " ADD " + sqlite + nicknamesColumnCurrent + " boolean NOT NULL DEFAULT 0",
                    "DROP TABLE IF EXISTS " + serverdataName
                };
                for (String query : queries) {
                    try {
                        query(query);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (usingMySQL) {
                        query("ALTER TABLE " + userName + " DROP INDEX " + userColumnPlayerKills);
                    }
                } catch (Exception e) {
                }
                setVersion(3);
            }

        }
        return true;
    }

    protected abstract Connection getNewConnection();

    public boolean query(String sql) throws SQLException {
        boolean success = connection.createStatement().execute(sql);
        return success;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public int getVersion() throws SQLException {
        int version = 0;
        ResultSet set = connection.prepareStatement("SELECT * FROM " + versionName).executeQuery();

        if (set.next()) {
            version = set.getInt("version");
        }

        set.close();

        return version;
    }

    @Override
    public void setVersion(int version) throws SQLException {
        connection.prepareStatement("DELETE FROM " + versionName).executeUpdate();
        connection.prepareStatement("INSERT INTO " + versionName + " (version) VALUES (" + version + ")").executeUpdate();

    }

    @Override
    public boolean wasSeenBefore(UUID uuid) {
        try {
            return getUserId(uuid.toString()) != -1;
        } catch (SQLException e) {
            plugin.toLog(this.getClass().getName(), e);
            return false;
        }
    }

    @Override
    public int getUserId(String uuid) throws SQLException {
        int userId = -1;
        try {
            checkConnection();
        } catch (Exception e) {
            plugin.toLog(this.getClass().getName(), e);
        }
        PreparedStatement statement = connection.prepareStatement("SELECT " + userColumnID + " FROM " + userName + " WHERE UPPER(" + userColumnUUID + ") LIKE UPPER(?)");
        statement.setString(1, uuid);
        ResultSet set = statement.executeQuery();
        while (set.next()) {
            userId = set.getInt(userColumnID);
        }
        set.close();
        return userId;
    }

    private UUID getUserUUID(String userID) throws SQLException {
        try {
            checkConnection();
        } catch (Exception e) {
            plugin.toLog(this.getClass().getName(), e);
        }
        UUID uuid = null;
        PreparedStatement statement = connection.prepareStatement("SELECT " + userColumnUUID + " FROM " + userName + " WHERE UPPER(" + userColumnID + ") LIKE UPPER(?)");
        statement.setString(1, userID);
        ResultSet set = statement.executeQuery();
        while (set.next()) {
            uuid = UUID.fromString(set.getString(userColumnUUID));
        }
        set.close();
        return uuid;
    }

    @Override
    public Set<UUID> getSavedUUIDs() throws SQLException {
        Set<UUID> uuids = new HashSet<>();
        PreparedStatement statement = connection.prepareStatement("SELECT " + userColumnUUID + " FROM " + userName);
        ResultSet set = statement.executeQuery();
        while (set.next()) {
            UUID uuid = UUID.fromString(set.getString(userColumnUUID));
            if (uuid == null) {
                continue;
            }
            uuids.add(uuid);
        }
        set.close();
        return uuids;
    }

    @Override
    public void saveCommandUse(HashMap<String, Integer> data) throws SQLException, NullPointerException {
        if (data.isEmpty()) {
            return;
        }
        PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + commanduseName);
        statement.execute();
        statement.close();

        statement = connection.prepareStatement("INSERT INTO " + commanduseName + " ("
                + commanduseColumnCommand + ", "
                + commanduseColumnTimesUsed
                + ") VALUES (?, ?)");
        boolean commitRequired = false;
        if (!data.isEmpty()) {
            for (String key : data.keySet()) {
                if (key.length() > 20) {
                    continue;
                }
                statement.setString(1, key);
                statement.setInt(2, data.get(key));
                statement.addBatch();
                commitRequired = true;
            }

            if (commitRequired) {
                statement.executeBatch();

            }
            statement.close();
        }
    }

    @Override
    public HashMap<String, Integer> getCommandUse() throws SQLException {
        HashMap<String, Integer> commandUse = new HashMap<>();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + commanduseName);

        ResultSet set = statement.executeQuery();

        while (set.next()) {
            commandUse.put(set.getString(commanduseColumnCommand), set.getInt(commanduseColumnTimesUsed));
        }
        set.close();
        statement.close();
        return commandUse;
    }

    @Override
    public boolean removeAccount(String uuid) throws SQLException {
        try {
            checkConnection();
        } catch (Exception e) {
            plugin.toLog(this.getClass().getName(), e);
            return false;
        }

        int userId = getUserId(uuid);
        if (userId == -1) {
            return false;
        }
        PreparedStatement statement;
        statement = connection.prepareStatement("DELETE FROM " + locationName + " WHERE UPPER(" + locationColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, "" + userId);
        statement.execute();
        statement.close();
        statement = connection.prepareStatement("DELETE FROM " + nicknamesName + " WHERE UPPER(" + nicknamesColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, "" + userId);
        statement.execute();
        statement.close();
        statement = connection.prepareStatement("DELETE FROM " + gamemodetimesName + " WHERE UPPER(" + gamemodetimesColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, "" + userId);
        statement.execute();
        statement.close();
        statement = connection.prepareStatement("DELETE FROM " + ipsName + " WHERE UPPER(" + ipsColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, "" + userId);
        statement.execute();
        statement.close();
        statement = connection.prepareStatement("DELETE FROM " + sessionName + " WHERE UPPER(" + sessionColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, "" + userId);
        statement.execute();
        statement.close();
        statement = connection.prepareStatement("DELETE FROM " + killsName + " WHERE " + killsColumnKillerUserID + " = ? OR " + killsColumnVictimUserID + " = ?");
        statement.setString(1, "" + userId);
        statement.setString(2, "" + userId);
        statement.execute();
        statement.close();
        statement = connection.prepareStatement("DELETE FROM " + userName + " WHERE UPPER(" + userColumnUUID + ") LIKE UPPER(?)");
        statement.setString(1, uuid);
        statement.execute();
        statement.close();

        return true;
    }

    @Override
    public void giveUserDataToProcessors(UUID uuid, Collection<DBCallableProcessor> processors) throws SQLException {
        try {
            checkConnection();
        } catch (Exception e) {
            plugin.toLog("Preparing for Exception report - Processors: " + processors.toString());
            plugin.toLog(this.getClass().getName(), e);
            return;
        }
        // Check if user is in the database
        if (!wasSeenBefore(uuid)) {
            return;
        }
        // Get the data
        UserData data = new UserData(getOfflinePlayer(uuid), new DemographicsData());

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + userName + " WHERE UPPER(" + userColumnUUID + ") LIKE UPPER(?)");
        statement.setString(1, uuid.toString());
        ResultSet set = statement.executeQuery();

        while (set.next()) {
            data.getDemData().setAge(set.getInt(userColumnDemAge));
            data.getDemData().setGender(Gender.parse(set.getString(userColumnDemGender)));
            data.getDemData().setGeoLocation(set.getString(userColumnDemGeoLocation));
            data.setLastGamemode(GameMode.valueOf(set.getString(userColumnLastGM)));
            data.setLastGmSwapTime(set.getLong(userColumnLastGMSwapTime));
            data.setPlayTime(set.getLong(userColumnPlayTime));
            data.setLoginTimes(set.getInt(userColumnLoginTimes));
            data.setLastPlayed(set.getLong(userColumnLastPlayed));
            data.setDeaths(set.getInt(userColumnDeaths));
            data.setMobKills(set.getInt(userColumnMobKills));
        }
        set.close();
        statement.close();
        String userId = "" + getUserId(uuid.toString());

        List<String> nicknames = getNicknames(userId);
        data.addNicknames(nicknames);
        if (nicknames.size() > 0) {
            data.setLastNick(nicknames.get(nicknames.size() - 1));
        }

        List<InetAddress> ips = getIPAddresses(userId);
        data.addIpAddresses(ips);

        HashMap<GameMode, Long> times = getGMTimes(userId);
        data.setGmTimes(times);

        data.addSessions(getSessionData(userId));
        data.setPlayerKills(getPlayerKills(userId));
        for (DBCallableProcessor processor : processors) {
            processor.process(data);
        }
    }

    private HashMap<GameMode, Long> getGMTimes(String userId) throws SQLException {
        PreparedStatement statement;
        ResultSet set;
        statement = connection.prepareStatement("SELECT * FROM " + gamemodetimesName + " WHERE UPPER(" + gamemodetimesColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, userId);
        set = statement.executeQuery();
        HashMap<GameMode, Long> times = new HashMap<>();
        while (set.next()) {
            times.put(GameMode.SURVIVAL, set.getLong(gamemodetimesColumnSurvivalTime));
            times.put(GameMode.CREATIVE, set.getLong(gamemodetimesColumnCreativeTime));
            times.put(GameMode.ADVENTURE, set.getLong(gamemodetimesColumnAdventureTime));
            try {
                times.put(GameMode.SPECTATOR, set.getLong(gamemodetimesColumnSpectatorTime));
            } catch (NoSuchFieldError e) {
            }
        }
        set.close();
        statement.close();
        return times;
    }

    private List<InetAddress> getIPAddresses(String userId) throws SQLException {
        PreparedStatement statement;
        ResultSet set;
        statement = connection.prepareStatement("SELECT * FROM " + ipsName + " WHERE UPPER(" + ipsColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, userId);
        set = statement.executeQuery();
        List<InetAddress> ips = new ArrayList<>();
        while (set.next()) {
            try {
                ips.add(InetAddress.getByName(set.getString(ipsColumnIP)));
            } catch (UnknownHostException e) {
            }
        }
        set.close();
        statement.close();
        return ips;
    }

    private List<SessionData> getSessionData(String userId) throws SQLException {
        PreparedStatement statement;
        ResultSet set;
        statement = connection.prepareStatement("SELECT * FROM " + sessionName + " WHERE UPPER(" + sessionColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, userId);
        set = statement.executeQuery();
        List<SessionData> sessions = new ArrayList<>();
        while (set.next()) {
            sessions.add(new SessionData(set.getLong(sessionColumnSessionStart), set.getLong(sessionColumnSessionEnd)));
        }
        set.close();
        statement.close();
        return sessions;
    }

    private List<String> getNicknames(String userId) throws SQLException {
        PreparedStatement statement;
        ResultSet set;
        statement = connection.prepareStatement("SELECT * FROM " + nicknamesName + " WHERE UPPER(" + nicknamesColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, userId);
        set = statement.executeQuery();
        List<String> nicknames = new ArrayList<>();
        String lastNick = "";
        while (set.next()) {
            String nickname = set.getString(nicknamesColumnNick);
            if (nickname.isEmpty()) {
                continue;
            }
            nicknames.add(nickname);
            if (set.getBoolean(nicknamesColumnCurrent)) {
                lastNick = nickname;
            }
        }
        if (!lastNick.isEmpty()) {
            nicknames.remove(lastNick);
            nicknames.add(lastNick);
        }
        set.close();
        statement.close();
        return nicknames;
    }

    private List<Location> getLocations(String userId, HashMap<String, World> worlds) throws SQLException {
        PreparedStatement statement;
        ResultSet set;
        statement = connection.prepareStatement("SELECT * FROM " + locationName + " WHERE UPPER(" + locationColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, userId);
        set = statement.executeQuery();
        List<Location> locations = new ArrayList<>();
        while (set.next()) {
            locations.add(new Location(worlds.get(set.getString(locationColumnWorld)), set.getInt(locationColumnCoordinatesX), 0, set.getInt(locationColumnCoordinatesZ)));
        }
        set.close();
        statement.close();
        return locations;
    }

    private List<KillData> getPlayerKills(String userId) throws SQLException {
        PreparedStatement statement;
        ResultSet set;
        statement = connection.prepareStatement("SELECT * FROM " + killsName + " WHERE UPPER(" + killsColumnKillerUserID + ") LIKE UPPER(?)");
        statement.setString(1, userId);
        set = statement.executeQuery();
        List<KillData> killData = new ArrayList<>();
        while (set.next()) {
            int victimID = set.getInt(killsColumnVictimUserID);
            UUID victimUUID = getUserUUID(victimID + "");
            killData.add(new KillData(victimUUID, victimID, set.getString(killsColumnWeapon), set.getLong(killsColumnDate)));
        }
        set.close();
        statement.close();
        return killData;
    }

    @Override
    public void saveMultipleUserData(List<UserData> data) throws SQLException {
        checkConnection();
        if (data.isEmpty()) {
            return;
        }
        Set<Exception> exceptions = new HashSet<>();
        List<UserData> saveLast = new ArrayList<>();
        String uSQL = "UPDATE " + userName + " SET "
                + userColumnDemAge + "=?, "
                + userColumnDemGender + "=?, "
                + userColumnDemGeoLocation + "=?, "
                + userColumnLastGM + "=?, "
                + userColumnLastGMSwapTime + "=?, "
                + userColumnPlayTime + "=?, "
                + userColumnLoginTimes + "=?, "
                + userColumnLastPlayed + "=?, "
                + userColumnDeaths + "=?, "
                + userColumnMobKills + "=? "
                + "WHERE " + userColumnUUID + "=?";

        try {
            PreparedStatement uStatement = connection.prepareStatement(uSQL);
            boolean commitRequired = false;
            for (UserData uData : data) {
                try {
                    if (uData == null) {
                        continue;
                    }
                    UUID uuid = uData.getUuid();
                    if (uuid == null) {
                        continue;
                    }
                    uData.access();

                    int userId = getUserId(uuid.toString());
                    if (userId == -1) {
                        saveLast.add(uData);
                        continue;
                    }

                    uStatement.setInt(1, uData.getDemData().getAge());
                    uStatement.setString(2, uData.getDemData().getGender().toString().toLowerCase());
                    uStatement.setString(3, uData.getDemData().getGeoLocation());
                    GameMode gm = uData.getLastGamemode();
                    if (gm != null) {
                        uStatement.setString(4, uData.getLastGamemode().name());
                    } else {
                        uStatement.setString(4, GameMode.SURVIVAL.name());
                    }
                    uStatement.setLong(5, uData.getLastGmSwapTime());
                    uStatement.setLong(6, uData.getPlayTime());
                    uStatement.setInt(7, uData.getLoginTimes());
                    uStatement.setLong(8, uData.getLastPlayed());
                    uStatement.setInt(9, uData.getDeaths());
                    uStatement.setInt(10, uData.getMobKills());
                    uStatement.setString(11, uData.getUuid().toString());
                    uStatement.addBatch();
                } catch (SQLException | NullPointerException e) {
                    saveLast.add(uData);
                    uData.stopAccessing();
                    exceptions.add(e);
                    continue;
                }
                uData.stopAccessing();
                commitRequired = true;
            }
            if (commitRequired) {
                uStatement.executeBatch();

            }
            uStatement.close();
            data.removeAll(saveLast);
        } catch (SQLException | IllegalStateException ex) {
            exceptions.add(ex);
        }
        for (UserData uData : data) {
            if (uData == null) {
                continue;
            }
            uData.access();
            try {
                int userId = getUserId(uData.getUuid().toString());
                saveAdditionalLocationsList(userId, uData.getLocations());
                saveNickList(userId, uData.getNicknames(), uData.getLastNick());
                saveIPList(userId, uData.getIps());
                saveSessionList(userId, uData.getSessions());
                savePlayerKills(userId, uData.getPlayerKills());

                saveGMTimes(userId, uData.getGmTimes());
            } catch (Exception e) {
                exceptions.add(e);
            }
            uData.stopAccessing();
        }
        for (UserData userData : saveLast) {
            UUID uuid = userData.getUuid();
            if (uuid == null) {
                continue;
            }
            try {
                saveUserData(uuid, userData);
            } catch (SQLException e) {
                exceptions.add(e);
            } catch (NullPointerException e) {
            }
        }
        if (!exceptions.isEmpty()) {
            plugin.logError("SEVERE: MULTIPLE ERRORS OCCURRED: " + exceptions.size());
            plugin.toLog(this.getClass().getName(), exceptions);
        }
    }

    @Override
    public void saveUserData(UUID uuid, UserData data) throws SQLException {
        if (uuid == null) {
            return;
        }
        checkConnection();
        data.access();
        int userId = getUserId(uuid.toString());
        int update = 0;
        if (userId != -1) {
            String sql = "UPDATE " + userName + " SET "
                    + userColumnDemAge + "=?, "
                    + userColumnDemGender + "=?, "
                    + userColumnDemGeoLocation + "=?, "
                    + userColumnLastGM + "=?, "
                    + userColumnLastGMSwapTime + "=?, "
                    + userColumnPlayTime + "=?, "
                    + userColumnLoginTimes + "=?, "
                    + userColumnLastPlayed + "=?, "
                    + userColumnDeaths + "=?, "
                    + userColumnMobKills + "=? "
                    + "WHERE UPPER(" + userColumnUUID + ") LIKE UPPER(?)";

            PreparedStatement statement = connection.prepareStatement(sql);
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
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + userName + " ("
                    + userColumnUUID + ", "
                    + userColumnDemAge + ", "
                    + userColumnDemGender + ", "
                    + userColumnDemGeoLocation + ", "
                    + userColumnLastGM + ", "
                    + userColumnLastGMSwapTime + ", "
                    + userColumnPlayTime + ", "
                    + userColumnLoginTimes + ", "
                    + userColumnLastPlayed + ", "
                    + userColumnDeaths + ", "
                    + userColumnMobKills
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
            statement.close();
            userId = getUserId(uuid.toString());
        }

        saveAdditionalLocationsList(userId, data.getLocations());
        saveNickList(userId, data.getNicknames(), data.getLastNick());
        saveIPList(userId, data.getIps());
        saveSessionList(userId, data.getSessions());
        savePlayerKills(userId, data.getPlayerKills());
        saveGMTimes(userId, data.getGmTimes());
        data.stopAccessing();
    }

    public void saveAdditionalLocationsList(int userId, List<Location> locations) throws SQLException {
        List<Location> newLocations = new ArrayList<>();
        newLocations.addAll(locations);
        if (newLocations.isEmpty()) {
            return;
        }
        PreparedStatement saveStatement = connection.prepareStatement("INSERT INTO " + locationName + " ("
                + locationColumnUserID + ", "
                + locationColumnCoordinatesX + ", "
                + locationColumnCoordinatesZ + ", "
                + locationColumnWorld
                + ") VALUES (?, ?, ?, ?)");
        boolean commitRequired = false;
        if (!newLocations.isEmpty()) {
            for (Location location : newLocations) {
                saveStatement.setInt(1, userId);
                saveStatement.setInt(2, (int) location.getBlockX());
                saveStatement.setInt(3, (int) location.getBlockZ());
                World world = location.getWorld();
                if (world == null) {
                    continue;
                }
                saveStatement.setString(4, world.getName());
                saveStatement.addBatch();
                commitRequired = true;
            }
            if (commitRequired) {
                saveStatement.executeBatch();

            }
        }
        saveStatement.close();
    }

    public void saveNickList(int userId, HashSet<String> names, String lastNick) throws SQLException {
        if (names.isEmpty()) {
            return;
        }
        PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + nicknamesName + " WHERE UPPER(" + nicknamesColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, "" + userId);
        statement.execute();

        statement = connection.prepareStatement("INSERT INTO " + nicknamesName + " ("
                + nicknamesColumnUserID + ", "
                + nicknamesColumnCurrent + ", "
                + nicknamesColumnNick
                + ") VALUES (?, ?, ?)");
        boolean commitRequired = false;
        for (String name : names) {
            statement.setInt(1, userId);
            statement.setInt(2, (name.equals(lastNick)) ? 1 : 0);
            statement.setString(3, name);
            statement.addBatch();
            commitRequired = true;
        }
        if (commitRequired) {
            statement.executeBatch();

        }
        statement.close();
    }

    public void saveSessionList(int userId, List<SessionData> sessions) throws SQLException {
        if (sessions.isEmpty()) {
            return;
        }
        PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + sessionName + " WHERE UPPER(" + sessionColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, "" + userId);
        statement.execute();

        statement = connection.prepareStatement("INSERT INTO " + sessionName + " ("
                + sessionColumnUserID + ", "
                + sessionColumnSessionStart + ", "
                + sessionColumnSessionEnd
                + ") VALUES (?, ?, ?)");
        boolean commitRequired = false;
        for (SessionData session : sessions) {
            statement.setInt(1, userId);
            statement.setLong(2, session.getSessionStart());
            statement.setLong(3, session.getSessionEnd());
            statement.addBatch();
            commitRequired = true;
        }
        if (commitRequired) {
            statement.executeBatch();

        }
        statement.close();
    }

    public void savePlayerKills(int userId, List<KillData> kills) throws SQLException {
        if (kills.isEmpty()) {
            return;
        }
        PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + killsName + " WHERE UPPER(" + killsColumnKillerUserID + ") LIKE UPPER(?)");
        statement.setString(1, "" + userId);
        statement.execute();

        statement = connection.prepareStatement("INSERT INTO " + killsName + " ("
                + killsColumnKillerUserID + ", "
                + killsColumnVictimUserID + ", "
                + killsColumnWeapon + ", "
                + killsColumnDate
                + ") VALUES (?, ?, ?, ?)");
        boolean commitRequired = false;
        for (KillData kill : kills) {
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
        statement.close();
    }

    public void saveIPList(int userId, HashSet<InetAddress> ips) throws SQLException {
        if (ips.isEmpty()) {
            return;
        }
        PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + ipsName + " WHERE UPPER(" + ipsColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, "" + userId);
        statement.execute();
        statement.close();

        statement = connection.prepareStatement("INSERT INTO " + ipsName + " ("
                + ipsColumnUserID + ", "
                + ipsColumnIP
                + ") VALUES (?, ?)");
        boolean commitRequired = false;
        for (InetAddress ip : ips) {
            statement.setInt(1, userId);
            statement.setString(2, ip.getHostAddress());
            statement.addBatch();
            commitRequired = true;
        }
        if (commitRequired) {
            statement.executeBatch();

        }
        statement.close();
    }

    public void saveGMTimes(int userId, HashMap<GameMode, Long> gamemodeTimes) throws SQLException {
        if (gamemodeTimes.isEmpty()) {
            return;
        }
        PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + gamemodetimesName + " WHERE UPPER(" + gamemodetimesColumnUserID + ") LIKE UPPER(?)");
        statement.setString(1, "" + userId);
        statement.execute();
        statement.close();

        statement = connection.prepareStatement("INSERT INTO " + gamemodetimesName + " ("
                + gamemodetimesColumnUserID + ", "
                + gamemodetimesColumnSurvivalTime + ", "
                + gamemodetimesColumnCreativeTime + ", "
                + gamemodetimesColumnAdventureTime + ", "
                + gamemodetimesColumnSpectatorTime
                + ") VALUES (?, ?, ?, ?, ?)");

        statement.setInt(1, userId);
        statement.setLong(2, gamemodeTimes.get(GameMode.SURVIVAL));
        statement.setLong(3, gamemodeTimes.get(GameMode.CREATIVE));
        statement.setLong(4, gamemodeTimes.get(GameMode.ADVENTURE));
        try {
            Long gm = gamemodeTimes.get(GameMode.SPECTATOR);
            if (gm != null) {
                statement.setLong(5, gm);
            } else {
                statement.setLong(5, 0);
            }
        } catch (NoSuchFieldError e) {
            statement.setLong(5, 0);
        }
        statement.execute();
        statement.close();

    }

    @Override
    public void clean() {
        try {
            checkConnection();
            query("DROP TABLE " + serverdataName);
        } catch (SQLException e) {
            plugin.toLog(this.getClass().getName(), e);
        }
    }

    @Override
    public boolean removeAllData() {
        try {
            checkConnection();
        } catch (SQLException e) {
            plugin.toLog(this.getClass().getName(), e);
            return false;
        }
        String[] queries = new String[]{
            locationName, ipsName, gamemodetimesName, nicknamesName, killsName, sessionName, commanduseName, userName
        };
        boolean success = true;
        for (String tableName : queries) {
            try {
                query("DELETE FROM " + tableName);
            } catch (SQLException e) {
                plugin.toLog(this.getClass().getName(), e);
                success = false;
            }
        }
        try {
            checkConnection();
        } catch (SQLException e) {
            plugin.toLog(this.getClass().getName(), e);
            return false;
        }
        return success;
    }

    public boolean supportsModification() {
        return supportsModification;
    }

    public Connection getConnection() {
        return connection;
    }
}
