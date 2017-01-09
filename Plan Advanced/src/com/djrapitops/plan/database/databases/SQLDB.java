package com.djrapitops.plan.database.databases;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.Gender;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.data.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class SQLDB extends Database {

    private final Plan plugin;

    private final boolean supportsModification;

    private Connection connection;

    private String userName;
    private String locationName;
    private String serverdataName;
    private String commanduseName;
    private String gamemodetimesName;
    private String nicknamesName;
    private String ipsName;

    private String userColumnUUID;
    private final String userColumnID;
    private String userColumnPlayTime;
    private String userColumnDemGeoLocation;
    private String userColumnDemAge;
    private String userColumnDemGender;
    private String userColumnLastGM;
    private String userColumnLastGMSwapTime;
    private String userColumnLoginTimes;
    private String userColumnLastPlayed;
    private final String locationColumnUserID;
    private String locationColumnID;
    private String locationColumnCoordinatesX;
    private String locationColumnCoordinatesZ;
    private String locationColumnWorld;
    private String serverdataColumnDate;
    private String serverdataColumnPlayersOnline;
    private String serverdataColumnNewPlayers;
    private String commanduseColumnCommand;
    private String commanduseColumnTimesUsed;
    private final String gamemodetimesColumnUserID;
    private String gamemodetimesColumnSurvivalTime;
    private String gamemodetimesColumnCreativeTime;
    private String gamemodetimesColumnAdventureTime;
    private String gamemodetimesColumnSpectatorTime;
    private String nicknamesColumnUserID;
    private String nicknamesColumnNick;
    private final String ipsColumnUserID;
    private String ipsColumnIP;

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

        userColumnID = "id";
        locationColumnID = "id";
        userColumnUUID = "uuid";
        locationColumnUserID = "user_id";
        nicknamesColumnUserID = "user_id";
        gamemodetimesColumnUserID = "user_id";
        ipsColumnUserID = "user_id";

        userColumnDemAge = "age";
        userColumnDemGender = "gender";
        userColumnDemGeoLocation = "geolocation";
        userColumnLastGM = "last_gamemode";
        userColumnLastGMSwapTime = "last_gamemode_swap";
        userColumnPlayTime = "play_time";
        userColumnLoginTimes = "login_times";
        userColumnLastPlayed = "last_played";

        locationColumnCoordinatesX = "x";
        locationColumnCoordinatesZ = "z";
        locationColumnWorld = "world_name";

        nicknamesColumnNick = "nickname";

        gamemodetimesColumnSurvivalTime = "survival";
        gamemodetimesColumnCreativeTime = "creative";
        gamemodetimesColumnAdventureTime = "adventure";
        gamemodetimesColumnSpectatorTime = "spectator";

        ipsColumnIP = "ip";

        commanduseColumnCommand = "command";
        commanduseColumnTimesUsed = "times_used";

        serverdataColumnDate = "date";
        serverdataColumnNewPlayers = "new_players";
        serverdataColumnPlayersOnline = "players_online";

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
        return checkConnection();

    }

    public boolean checkConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = getNewConnection();

                if (connection == null || connection.isClosed()) {
                    return false;
                }

                ResultSet set = connection.prepareStatement(supportsModification ? ("SHOW TABLES LIKE '" + userName + "'") : "SELECT name FROM sqlite_master WHERE type='table' AND name='" + userName + "'").executeQuery();

//                boolean newDatabase = set.next();
                set.close();

                query("CREATE TABLE IF NOT EXISTS " + userName + " ("
                        + userColumnID + " integer PRIMARY KEY, "
                        + userColumnUUID + " varchar(36) NOT NULL, "
                        + userColumnDemAge + " integer NOT NULL, "
                        + userColumnDemGender + " varchar(8) NOT NULL, "
                        + userColumnDemGeoLocation + " varchar(50) NOT NULL, "
                        + userColumnLastGM + " varchar(15) NOT NULL, "
                        + userColumnLastGMSwapTime + " bigint NOT NULL, "
                        + userColumnPlayTime + " bigint NOT NULL, "
                        + userColumnLoginTimes + " integer NOT NULL, "
                        + userColumnLastPlayed + " bigint NOT NULL"
                        + ")"
                );
                /* Locations Removed from Build 2.0.0 for performance reasons.
                query("CREATE TABLE IF NOT EXISTS " + locationName + " ("
                        + locationColumnID + " integer PRIMARY KEY, "
                        + locationColumnUserID + " integer NOT NULL, "
                        + locationColumnCoordinatesX + " integer NOT NULL, "
                        + locationColumnCoordinatesZ + " integer NOT NULL, "
                        + locationColumnWorld + " varchar(64) NOT NULL, "
                        + "FOREIGN KEY(" + locationColumnUserID + ") REFERENCES " + userName + "(" + userColumnID + ")"
                        + ")"
                );
                */
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
                        + nicknamesColumnNick + " varchar(30) NOT NULL, "
                        + "FOREIGN KEY(" + nicknamesColumnUserID + ") REFERENCES " + userName + "(" + userColumnID + ")"
                        + ")"
                );

                query("CREATE TABLE IF NOT EXISTS " + commanduseName + " ("
                        + commanduseColumnCommand + " varchar(20) NOT NULL, "
                        + commanduseColumnTimesUsed + " integer NOT NULL"
                        + ")"
                );

                query("CREATE TABLE IF NOT EXISTS " + serverdataName + " ("
                        + serverdataColumnDate + " bigint NOT NULL, "
                        + serverdataColumnNewPlayers + " integer NOT NULL, "
                        + serverdataColumnPlayersOnline + " integer NOT NULL"
                        + ")"
                );

                query("CREATE TABLE IF NOT EXISTS " + versionName + " ("
                        + "version integer NOT NULL"
                        + ")"
                );

            }
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    protected abstract Connection getNewConnection();

    public boolean query(String sql) throws SQLException {
        return connection.createStatement().execute(sql);
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getVersion() {
        checkConnection();

        int version = 0;

        try {
            ResultSet set = connection.prepareStatement("SELECT * from " + versionName).executeQuery();

            if (set.next()) {
                version = set.getInt("version");
            }

            set.close();

            return version;
        } catch (Exception e) {
            e.printStackTrace();

            return version;
        }
    }

    @Override
    public void setVersion(int version) {
        checkConnection();

        try {
            connection.prepareStatement("DELETE FROM " + versionName).executeUpdate();

            connection.prepareStatement("INSERT INTO " + versionName + " (version) VALUES (" + version + ")").executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean wasSeenBefore(UUID uuid) {
        return getUserId(uuid.toString()) != -1;
    }

    public int getUserId(String uuid) {
        int userId = -1;
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT " + userColumnID + " FROM " + userName + " WHERE UPPER(" + userColumnUUID + ") LIKE UPPER(?)");
            statement.setString(1, uuid);
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                userId = set.getInt(userColumnID);
            }
            set.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    @Override
    public UserData getUserData(UUID uuid) {
        checkConnection();
        // Check if user is in the database
        if (!wasSeenBefore(uuid)) {
            return null;
        }
        List<World> worldList = Bukkit.getServer().getWorlds();
        World defaultWorld = worldList.get(0);
        HashMap<String, World> worlds = new HashMap<>();
        for (World w : worldList) {
            worlds.put(w.getName(), w);
        }
        // Get the data
        UserData data = new UserData(getOfflinePlayer(uuid), new DemographicsData(), this);
        try {
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
            }
            set.close();
            statement.close();
            String userId = "" + getUserId(uuid.toString());

            statement = connection.prepareStatement("SELECT * FROM " + locationName + " WHERE UPPER(" + locationColumnUserID + ") LIKE UPPER(?)");
            statement.setString(1, userId);
            set = statement.executeQuery();

            /* Locations Removed from Build 2.0.0 for performance reasons.
            List<Location> locations = new ArrayList<>();
            while (set.next()) {
                locations.add(new Location(worlds.get(set.getString(locationColumnWorld)), set.getInt(locationColumnCoordinatesX), 0, set.getInt(locationColumnCoordinatesZ)));
            }
            set.close();
            statement.close();
            data.addLocations(locations);
            
            if (locations.isEmpty()) {
                data.setLocation(new Location(defaultWorld, 0, 0, 0));
            } else {
                data.setLocation(locations.get(locations.size() - 1));
            }
            */
            data.setLocation(new Location(defaultWorld, 0, 0, 0));

            statement = connection.prepareStatement("SELECT * FROM " + nicknamesName + " WHERE UPPER(" + nicknamesColumnUserID + ") LIKE UPPER(?)");
            statement.setString(1, userId);
            set = statement.executeQuery();

            List<String> nicknames = new ArrayList<>();
            while (set.next()) {
                nicknames.add(set.getString(nicknamesColumnNick));
            }
            set.close();
            statement.close();
            data.addNicknames(nicknames);

            statement = connection.prepareStatement("SELECT * FROM " + ipsName + " WHERE UPPER(" + ipsColumnUserID + ") LIKE UPPER(?)");
            statement.setString(1, userId);
            set = statement.executeQuery();

            List<InetAddress> ips = new ArrayList<>();
            while (set.next()) {
                try {
                    ips.add(InetAddress.getByName(set.getString(ipsColumnIP)));
                } catch (SQLException | UnknownHostException e) {
                }
            }
            set.close();
            statement.close();
            data.addIpAddresses(ips);

            statement = connection.prepareStatement("SELECT * FROM " + gamemodetimesName + " WHERE UPPER(" + gamemodetimesColumnUserID + ") LIKE UPPER(?)");
            statement.setString(1, userId);
            set = statement.executeQuery();

            HashMap<GameMode, Long> times = new HashMap<>();
            while (set.next()) {
                times.put(GameMode.SURVIVAL, set.getLong(gamemodetimesColumnSurvivalTime));
                times.put(GameMode.CREATIVE, set.getLong(gamemodetimesColumnCreativeTime));
                times.put(GameMode.ADVENTURE, set.getLong(gamemodetimesColumnAdventureTime));
                times.put(GameMode.SPECTATOR, set.getLong(gamemodetimesColumnSpectatorTime));
            }
            set.close();
            statement.close();
            data.setGmTimes(times);
        } catch (SQLException e) {
            data = null;
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public ServerData getNewestServerData() {
        HashMap<String, Integer> commandUse = getCommandUse();
        int newPlayers = 0;
        Date now = new Date();
        Date startOfToday = new Date(now.getTime() - (now.getTime() % 86400000));
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + serverdataName
                    + " ORDER BY " + serverdataColumnDate + " ASC LIMIT 1");

            ResultSet set = statement.executeQuery();
            while (set.next()) {
                Date lastSave = new Date(set.getLong(serverdataColumnDate));
                Date startOfSaveDay = new Date(lastSave.getTime() - (lastSave.getTime() % 86400000));
                if (startOfSaveDay == startOfToday) {
                    newPlayers = set.getInt(serverdataColumnNewPlayers);
                }
            }
            set.close();
            statement.close();
        } catch (SQLException e) {
            plugin.logToFile("DATABASE-SQLDB-GetServerData\n" + e + "\n" + e.getCause());
        }
        return new ServerData(commandUse, newPlayers);
    }

    @Override
    public void saveServerData(ServerData data) {
        try {
            saveCommandUse(data.getCommandUsage());
            long now = new Date().getTime();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + serverdataName + " ("
                    + serverdataColumnDate + ", "
                    + serverdataColumnNewPlayers + ", "
                    + serverdataColumnPlayersOnline
                    + ") VALUES (?, ?, ?)");

            statement.setLong(1, now);
            statement.setInt(2, data.getNewPlayers());
            statement.setInt(3, data.getPlayersOnline());
            statement.execute();
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(SQLDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void saveCommandUse(HashMap<String, Integer> data) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + commanduseName);
            statement.execute();
            statement.close();
            for (String key : data.keySet()) {
                statement = connection.prepareStatement("INSERT INTO " + commanduseName + " ("
                        + commanduseColumnCommand + ", "
                        + commanduseColumnTimesUsed
                        + ") VALUES (?, ?)");

                statement.setString(1, key);
                statement.setInt(2, data.get(key));
                statement.execute();
                statement.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Integer> getCommandUse() {
        HashMap<String, Integer> commandUse = new HashMap<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + commanduseName);

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                commandUse.put(set.getString(commanduseColumnCommand), set.getInt(commanduseColumnTimesUsed));
            }
            set.close();
            statement.close();
        } catch (SQLException e) {
            plugin.logToFile("DATABASE-SQLDB\n" + e + "\n" + e.getCause());
        }
        return commandUse;
    }

    public void removeAccount(String uuid) {

        checkConnection();

        int userId = getUserId(uuid);
        if (userId == -1) {
            return;
        }
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement("DELETE FROM " + userName + " WHERE UPPER(" + userColumnUUID + ") LIKE UPPER(?)");
            statement.setString(1, uuid);
            statement.execute();
            statement.close();
            /* Locations Removed from Build 2.0.0 for performance reasons.
            statement = connection.prepareStatement("DELETE FROM " + locationName + " WHERE UPPER(" + locationColumnUserID + ") LIKE UPPER(?)");
            statement.setString(1, "" + userId);
            statement.execute();
            statement.close();
            */
            statement = connection.prepareStatement("DELETE FROM " + nicknamesName + " WHERE UPPER(" + nicknamesColumnUserID + ") LIKE UPPER(?)");
            statement.setString(1, "" + userId);
            statement.execute();
            statement.close();
            statement = connection.prepareStatement("DELETE FROM " + gamemodetimesName + " WHERE UPPER(" + gamemodetimesColumnUserID + ") LIKE UPPER(?)");
            statement.setString(1, "" + userId);
            statement.execute();
            statement.close();
            statement = connection.prepareStatement("DELETE FROM " + ipsName + " WHERE UPPER(" + ipsColumnIP + ") LIKE UPPER(?)");
            statement.setString(1, "" + userId);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            plugin.logToFile("DATABASE_SQLDB\n" + e + "\n" + e.getCause());
        }
    }

    @Override
    public void saveUserData(UUID uuid, UserData data) {
        checkConnection();
        data.setAccessing(true);
        int userId = getUserId(uuid.toString());
        try {
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
                        + userColumnLastPlayed + "=? "
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
                statement.setString(9, uuid.toString());
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
                        + userColumnLastPlayed
                        + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

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

                statement.execute();
                statement.close();
                userId = getUserId(uuid.toString());
            }
            // saveLocationList(userId, data.getLocations());
            saveNickList(userId, data.getNicknames());
            saveIPList(userId, data.getIps());
            saveGMTimes(userId, data.getGmTimes());

        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            plugin.logToFile("SQLDB-Save\n" + e
                    + "\n" + data.getLastGamemode()
                    + "\n" + Bukkit.getDefaultGameMode()
                    + "\n" + wasSeenBefore(uuid)
            );
        }
        data.setAccessing(false);
    }

    @Deprecated // Locations Removed from Build 2.0.0 for performance reasons.
    public void saveLocationList(int userId, List<Location> locations) {
        /*
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + locationName + " WHERE UPPER(" + locationColumnUserID + ") LIKE UPPER(?)");
            statement.setString(1, "" + userId);
            statement.execute();
            statement.close();

            for (Location location : locations) {
                PreparedStatement saveStatement = connection.prepareStatement("INSERT INTO " + locationName + " ("
                        + locationColumnUserID + ", "
                        + locationColumnCoordinatesX + ", "
                        + locationColumnCoordinatesZ + ", "
                        + locationColumnWorld
                        + ") VALUES (?, ?, ?, ?)");

                saveStatement.setInt(1, userId);
                saveStatement.setInt(2, (int) location.getBlockX());
                saveStatement.setInt(3, (int) location.getBlockZ());
                saveStatement.setString(4, location.getWorld().getName());

                saveStatement.execute();
                saveStatement.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        */
    }

    public void saveNickList(int userId, HashSet<String> names) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + nicknamesName + " WHERE UPPER(" + nicknamesColumnUserID + ") LIKE UPPER(?)");
            statement.setString(1, "" + userId);
            statement.execute();

            for (String name : names) {
                statement = connection.prepareStatement("INSERT INTO " + nicknamesName + " ("
                        + nicknamesColumnUserID + ", "
                        + nicknamesColumnNick
                        + ") VALUES (?, ?)");

                statement.setInt(1, userId);
                statement.setString(2, name);
                statement.execute();
                statement.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveIPList(int userId, HashSet<InetAddress> ips) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + ipsName + " WHERE UPPER(" + ipsColumnUserID + ") LIKE UPPER(?)");
            statement.setString(1, "" + userId);
            statement.execute();
            statement.close();

            for (InetAddress ip : ips) {
                statement = connection.prepareStatement("INSERT INTO " + ipsName + " ("
                        + ipsColumnUserID + ", "
                        + ipsColumnIP
                        + ") VALUES (?, ?)");

                statement.setInt(1, userId);
                statement.setString(2, ip.getHostAddress());
                statement.execute();
                statement.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveGMTimes(int userId, HashMap<GameMode, Long> gamemodeTimes) {
        try {
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
            statement.setLong(5, gamemodeTimes.get(GameMode.SPECTATOR));
            statement.execute();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clean() {
        checkConnection();
        plugin.log("DATABASE-SQLDB\nCleaning DB not implemented");
    }

    public void removeAllData() {
        checkConnection();

        try {
            connection.prepareStatement("DELETE FROM " + userName).executeUpdate();
            connection.prepareStatement("DELETE FROM " + locationName).executeUpdate();
            connection.prepareStatement("DELETE FROM " + nicknamesName).executeUpdate();
            connection.prepareStatement("DELETE FROM " + ipsName).executeUpdate();
            connection.prepareStatement("DELETE FROM " + gamemodetimesName).executeUpdate();
            connection.prepareStatement("DELETE FROM " + serverdataName).executeUpdate();
            connection.prepareStatement("DELETE FROM " + commanduseName).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Setters ---
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setServerdataName(String serverdataName) {
        this.serverdataName = serverdataName;
    }

    public void setCommanduseName(String commanduseName) {
        this.commanduseName = commanduseName;
    }

    public void setGamemodetimesName(String gamemodetimesName) {
        this.gamemodetimesName = gamemodetimesName;
    }

    public void setNicknamesName(String nicknamesName) {
        this.nicknamesName = nicknamesName;
    }

    public void setIpsName(String ipsName) {
        this.ipsName = ipsName;
    }

    public void setUserColumnUUID(String userColumnUUID) {
        this.userColumnUUID = userColumnUUID;
    }

    public void setUserColumnPlayTime(String userColumnPlayTime) {
        this.userColumnPlayTime = userColumnPlayTime;
    }

    public void setUserColumnDemGeoLocation(String userColumnDemGeoLocation) {
        this.userColumnDemGeoLocation = userColumnDemGeoLocation;
    }

    public void setUserColumnDemAge(String userColumnDemAge) {
        this.userColumnDemAge = userColumnDemAge;
    }

    public void setUserColumnDemGender(String userColumnDemGender) {
        this.userColumnDemGender = userColumnDemGender;
    }

    public void setUserColumnLastGM(String userColumnLastGM) {
        this.userColumnLastGM = userColumnLastGM;
    }

    public void setUserColumnLastGMSwapTime(String userColumnLastGMSwapTime) {
        this.userColumnLastGMSwapTime = userColumnLastGMSwapTime;
    }

    public void setLocationColumnCoordinatesZ(String locationColumnCoordinates) {
        this.locationColumnCoordinatesZ = locationColumnCoordinates;
    }

    public void setLocationColumnCoordinatesX(String locationColumnCoordinates) {
        this.locationColumnCoordinatesX = locationColumnCoordinates;
    }

    public void setLocationColumnWorld(String locationColumnWorld) {
        this.locationColumnWorld = locationColumnWorld;
    }

    public void setServerdataColumnDate(String serverdataColumnDate) {
        this.serverdataColumnDate = serverdataColumnDate;
    }

    public void setServerdataColumnPlayersOnline(String serverdataColumnPlayersOnline) {
        this.serverdataColumnPlayersOnline = serverdataColumnPlayersOnline;
    }

    public void setServerdataColumnNewPlayers(String serverdataColumnNewPlayers) {
        this.serverdataColumnNewPlayers = serverdataColumnNewPlayers;
    }

    public void setCommanduseColumnCommand(String commanduseColumnCommand) {
        this.commanduseColumnCommand = commanduseColumnCommand;
    }

    public void setCommanduseColumnTimesUsed(String commanduseColumnTimesUsed) {
        this.commanduseColumnTimesUsed = commanduseColumnTimesUsed;
    }

    public void setUserColumnLoginTimes(String userColumnLoginTimes) {
        this.userColumnLoginTimes = userColumnLoginTimes;
    }

    public void setGamemodetimesColumnSurvivalTime(String gamemodetimesColumnSurvivalTime) {
        this.gamemodetimesColumnSurvivalTime = gamemodetimesColumnSurvivalTime;
    }

    public void setGamemodetimesColumnCreativeTime(String gamemodetimesColumnCreativeTime) {
        this.gamemodetimesColumnCreativeTime = gamemodetimesColumnCreativeTime;
    }

    public void setGamemodetimesColumnAdventureTime(String gamemodetimesColumnAdventureTime) {
        this.gamemodetimesColumnAdventureTime = gamemodetimesColumnAdventureTime;
    }

    public void setGamemodetimesColumnSpectatorTime(String gamemodetimesColumnSpectatorTime) {
        this.gamemodetimesColumnSpectatorTime = gamemodetimesColumnSpectatorTime;
    }

    public void setNicknamesColumnUserID(String nicknamesColumnUserID) {
        this.nicknamesColumnUserID = nicknamesColumnUserID;
    }

    public void setNicknamesColumnNick(String nicknamesColumnNick) {
        this.nicknamesColumnNick = nicknamesColumnNick;
    }

    public void setIpsColumnIP(String ipsColumnIP) {
        this.ipsColumnIP = ipsColumnIP;
    }

    // Getters
    public boolean supportsModification() {
        return supportsModification;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getUserName() {
        return userName;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getServerdataName() {
        return serverdataName;
    }

    public String getCommanduseName() {
        return commanduseName;
    }

    public String getGamemodetimesName() {
        return gamemodetimesName;
    }

    public String getNicknamesName() {
        return nicknamesName;
    }

    public String getIpsName() {
        return ipsName;
    }

    public String getUserColumnUUID() {
        return userColumnUUID;
    }

    public String getUserColumnID() {
        return userColumnID;
    }

    public String getUserColumnPlayTime() {
        return userColumnPlayTime;
    }

    public String getUserColumnDemGeoLocation() {
        return userColumnDemGeoLocation;
    }

    public String getUserColumnDemAge() {
        return userColumnDemAge;
    }

    public String getUserColumnDemGender() {
        return userColumnDemGender;
    }

    public String getUserColumnLastGM() {
        return userColumnLastGM;
    }

    public String getUserColumnLastGMSwapTime() {
        return userColumnLastGMSwapTime;
    }

    public String getUserColumnLoginTimes() {
        return userColumnLoginTimes;
    }

    public String getUserColumnLastPlayed() {
        return userColumnLastPlayed;
    }

    public String getLocationColumnUserID() {
        return locationColumnUserID;
    }

    public String getLocationColumnID() {
        return locationColumnID;
    }

    public String getLocationColumnCoordinatesX() {
        return locationColumnCoordinatesX;
    }

    public String getLocationColumnCoordinatesZ() {
        return locationColumnCoordinatesZ;
    }

    public String getLocationColumnWorld() {
        return locationColumnWorld;
    }

    public String getServerdataColumnDate() {
        return serverdataColumnDate;
    }

    public String getServerdataColumnPlayersOnline() {
        return serverdataColumnPlayersOnline;
    }

    public String getServerdataColumnNewPlayers() {
        return serverdataColumnNewPlayers;
    }

    public String getCommanduseColumnCommand() {
        return commanduseColumnCommand;
    }

    public String getCommanduseColumnTimesUsed() {
        return commanduseColumnTimesUsed;
    }

    public String getGamemodetimesColumnUserID() {
        return gamemodetimesColumnUserID;
    }

    public String getGamemodetimesColumnSurvivalTime() {
        return gamemodetimesColumnSurvivalTime;
    }

    public String getGamemodetimesColumnCreativeTime() {
        return gamemodetimesColumnCreativeTime;
    }

    public String getGamemodetimesColumnAdventureTime() {
        return gamemodetimesColumnAdventureTime;
    }

    public String getGamemodetimesColumnSpectatorTime() {
        return gamemodetimesColumnSpectatorTime;
    }

    public String getNicknamesColumnUserID() {
        return nicknamesColumnUserID;
    }

    public String getNicknamesColumnNick() {
        return nicknamesColumnNick;
    }

    public String getIpsColumnUserID() {
        return ipsColumnUserID;
    }

    public String getIpsColumnIP() {
        return ipsColumnIP;
    }

    public String getVersionName() {
        return versionName;
    }
}
