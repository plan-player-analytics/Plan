package main.java.com.djrapitops.plan.database.databases;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.*;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.*;
import main.java.com.djrapitops.plan.utilities.UUIDFetcher;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import static org.bukkit.Bukkit.getOfflinePlayer;

/**
 *
 * @author Rsl1122
 */
public abstract class SQLDB extends Database {

    final Plan plugin;

    private final boolean supportsModification;

    private Connection connection;

    private final UsersTable usersTable;
    private final GMTimesTable gmTimesTable;
    private final KillsTable killsTable;
    private final LocationsTable locationsTable;
    private final NicknamesTable nicknamesTable;
    private final SessionsTable sessionsTable;
    private final IPsTable ipsTable;
    private final CommandUseTable commandUseTable;
    private final VersionTable versionTable;

    /**
     *
     * @param plugin
     * @param supportsModification
     */
    public SQLDB(Plan plugin, boolean supportsModification) {
        super(plugin);
        this.plugin = plugin;
        this.supportsModification = supportsModification;
        boolean usingMySQL = getName().equals("MySQL");

        usersTable = new UsersTable(this, usingMySQL);
        gmTimesTable = new GMTimesTable(this, usingMySQL);
        sessionsTable = new SessionsTable(this, usingMySQL);
        killsTable = new KillsTable(this, usingMySQL);
        locationsTable = new LocationsTable(this, usingMySQL);
        ipsTable = new IPsTable(this, usingMySQL);
        nicknamesTable = new NicknamesTable(this, usingMySQL);
        commandUseTable = new CommandUseTable(this, usingMySQL);
        versionTable = new VersionTable(this, usingMySQL);

        startConnectionPingTask(plugin);
    }

    /**
     *
     * @param plugin
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    public void startConnectionPingTask(Plan plugin) throws IllegalArgumentException, IllegalStateException {
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

    /**
     *
     * @return
     */
    @Override
    public boolean init() {
        super.init();
        try {
            return checkConnection();
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
            return false;
        }
    }

    /**
     *
     * @return @throws SQLException
     */
    public boolean checkConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = getNewConnection();

            if (connection == null || connection.isClosed()) {
                return false;
            }

            boolean newDatabase = true;
            try {
                getVersion();
                newDatabase = false;
            } catch (Exception e) {
            }
            versionTable.createTable();
            if (newDatabase) {
                Log.info("New Database created.");
                setVersion(3);
            }
            for (Table table : getAllTables()) {
                table.createTable();
            }
        }
        return true;
    }

    /**
     *
     * @return
     */
    public Table[] getAllTables() {
        return new Table[]{usersTable, locationsTable, gmTimesTable, ipsTable, nicknamesTable, sessionsTable, killsTable, commandUseTable};
    }

    /**
     *
     * @return
     */
    public Table[] getAllTablesInRemoveOrder() {
        return new Table[]{locationsTable, gmTimesTable, ipsTable, nicknamesTable, sessionsTable, killsTable, usersTable, commandUseTable};
    }

    /**
     *
     * @return
     */
    public abstract Connection getNewConnection();

    /**
     *
     * @throws SQLException
     */
    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     *
     * @return @throws SQLException
     */
    @Override
    public int getVersion() throws SQLException {
        return versionTable.getVersion();
    }

    /**
     *
     * @param version
     * @throws SQLException
     */
    @Override
    public void setVersion(int version) throws SQLException {
        versionTable.setVersion(version);
    }

    /**
     *
     * @param uuid
     * @return
     */
    @Override
    public boolean wasSeenBefore(UUID uuid) {
        try {
            return getUserId(uuid.toString()) != -1;
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
            return false;
        }
    }

    /**
     *
     * @param uuid
     * @return
     * @throws SQLException
     */
    @Override
    public int getUserId(String uuid) throws SQLException {
        return usersTable.getUserId(uuid);
    }

    /**
     *
     * @return @throws SQLException
     */
    @Override
    public Set<UUID> getSavedUUIDs() throws SQLException {
        return usersTable.getSavedUUIDs();
    }

    /**
     *
     * @param commandUse
     * @throws SQLException
     * @throws NullPointerException
     */
    @Override
    @Deprecated
    public void saveCommandUse(HashMap<String, Integer> commandUse) throws SQLException, NullPointerException {
        commandUseTable.saveCommandUse(commandUse);
    }

    /**
     *
     * @return @throws SQLException
     */
    @Override
    @Deprecated
    public HashMap<String, Integer> getCommandUse() throws SQLException {
        return commandUseTable.getCommandUse();
    }

    /**
     *
     * @param uuid
     * @return
     * @throws SQLException
     */
    @Override
    public boolean removeAccount(String uuid) throws SQLException {
        try {
            checkConnection();
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            return false;
        }
        int userId = getUserId(uuid);
        if (userId == -1) {
            return false;
        }
        return locationsTable.removeUserLocations(userId)
                && ipsTable.removeUserIps(userId)
                && nicknamesTable.removeUserNicknames(userId)
                && gmTimesTable.removeUserGMTimes(userId)
                && sessionsTable.removeUserSessions(userId)
                && killsTable.removeUserKillsAndVictims(userId)
                && usersTable.removeUser(uuid);
    }

    /**
     *
     * @param uuid
     * @param processors
     * @throws SQLException
     */
    @Override
    public void giveUserDataToProcessors(UUID uuid, Collection<DBCallableProcessor> processors) throws SQLException {
        try {
            checkConnection();
        } catch (Exception e) {
            Log.toLog("Preparing for Exception report - Processors: " + processors.toString());
            Log.toLog(this.getClass().getName(), e);
            return;
        }
        // Check if user is in the database
        if (!wasSeenBefore(uuid)) {
            return;
        }
        // Get the data
        UserData data = new UserData(getOfflinePlayer(uuid), new DemographicsData());
        usersTable.addUserInformationToUserData(data);

        int userId = getUserId(uuid.toString());

        List<String> nicknames = nicknamesTable.getNicknames(userId);
        data.addNicknames(nicknames);
        if (nicknames.size() > 0) {
            data.setLastNick(nicknames.get(nicknames.size() - 1));
        }

        List<InetAddress> ips = ipsTable.getIPAddresses(userId);
        data.addIpAddresses(ips);

        HashMap<GameMode, Long> times = gmTimesTable.getGMTimes(userId);
        data.setGmTimes(times);
        List<SessionData> sessions = sessionsTable.getSessionData(userId);
        data.addSessions(sessions);
        data.setPlayerKills(killsTable.getPlayerKills(userId));
        for (DBCallableProcessor processor : processors) {
            processor.process(data);
        }
    }

    @Deprecated
    private HashMap<GameMode, Long> getGMTimes(int userId) throws SQLException {
        return gmTimesTable.getGMTimes(userId);
    }

    @Deprecated
    private List<InetAddress> getIPAddresses(int userId) throws SQLException {
        return ipsTable.getIPAddresses(userId);
    }

    @Deprecated
    private List<String> getNicknames(int userId) throws SQLException {
        return nicknamesTable.getNicknames(userId);
    }

    @Override
    @Deprecated
    public List<Location> getLocations(String userId, HashMap<String, World> worlds) throws SQLException {
        return getLocations(Integer.parseInt(userId), worlds);
    }

    /**
     *
     * @param userId
     * @param worlds
     * @return
     * @throws SQLException
     * @deprecated
     */
    @Deprecated
    public List<Location> getLocations(int userId, HashMap<String, World> worlds) throws SQLException {
        return locationsTable.getLocations(userId, worlds);
    }

    @Deprecated
    private List<KillData> getPlayerKills(int userId) throws SQLException {
        return killsTable.getPlayerKills(userId);
    }

    /**
     *
     * @param data
     * @throws SQLException
     */
    @Override
    public void saveMultipleUserData(List<UserData> data) throws SQLException {
        checkConnection();
        if (data.isEmpty()) {
            return;
        }
        Set<Throwable> exceptions = new HashSet<>();
        List<UserData> saveLast = usersTable.saveUserDataInformationBatch(data);
        data.removeAll(saveLast);
        for (UserData uData : data) {
            if (uData == null) {
                continue;
            }
            UUID uuid = uData.getUuid();
            if (uuid == null) {
                try {
                    uData.setUuid(UUIDFetcher.getUUIDOf(uData.getName()));
                    if (uData.getUuid() == null) {
                        continue;
                    }
                } catch (Exception ex) {
                    continue;
                }
            }
            uData.access();
            try {
                int userId = getUserId(uData.getUuid().toString());
                sessionsTable.saveSessionData(userId, uData.getSessions());
                saveAdditionalLocationsList(userId, uData.getLocations());
                saveNickList(userId, uData.getNicknames(), uData.getLastNick());
                saveIPList(userId, uData.getIps());
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
            Log.error("SEVERE: MULTIPLE ERRORS OCCURRED: " + exceptions.size());
            Log.toLog(this.getClass().getName(), exceptions);
        }
    }

    /**
     *
     * @param uuid
     * @param data
     * @throws SQLException
     */
    @Override
    public void saveUserData(UUID uuid, UserData data) throws SQLException {
        if (uuid == null) {
            return;
        }
        checkConnection();
        Log.debug("DB_Save: " + data);
        data.access();
        usersTable.saveUserDataInformation(data);
        int userId = getUserId(uuid.toString());
        sessionsTable.saveSessionData(userId, data.getSessions());
        locationsTable.saveAdditionalLocationsList(userId, data.getLocations());
        nicknamesTable.saveNickList(userId, data.getNicknames(), data.getLastNick());
        ipsTable.saveIPList(userId, data.getIps());
        killsTable.savePlayerKills(userId, data.getPlayerKills());
        gmTimesTable.saveGMTimes(userId, data.getGmTimes());
        data.stopAccessing();
    }

    /**
     *
     * @param userId
     * @param locations
     * @throws SQLException
     */
    @Deprecated
    public void saveAdditionalLocationsList(int userId, List<Location> locations) throws SQLException {
        locationsTable.saveAdditionalLocationsList(userId, locations);
    }

    /**
     *
     * @param userId
     * @param names
     * @param lastNick
     * @throws SQLException
     */
    @Deprecated
    public void saveNickList(int userId, Set<String> names, String lastNick) throws SQLException {
        nicknamesTable.saveNickList(userId, names, lastNick);
    }

    /**
     *
     * @param userId
     * @param sessions
     * @throws SQLException
     * @deprecated Use sessionsTable instead.
     */
    @Deprecated
    public void saveSessionList(int userId, List<SessionData> sessions) throws SQLException {
        sessionsTable.saveSessionData(userId, sessions);
    }

    /**
     *
     * @param userId
     * @param kills
     * @throws SQLException
     */
    @Deprecated
    public void savePlayerKills(int userId, List<KillData> kills) throws SQLException {
        killsTable.savePlayerKills(userId, kills);
    }

    /**
     *
     * @param userId
     * @param ips
     * @throws SQLException
     */
    @Deprecated
    public void saveIPList(int userId, Set<InetAddress> ips) throws SQLException {
        ipsTable.saveIPList(userId, ips);
    }

    /**
     *
     * @param userId
     * @param gamemodeTimes
     * @throws SQLException
     */
    @Deprecated
    public void saveGMTimes(int userId, Map<GameMode, Long> gamemodeTimes) throws SQLException {
        gmTimesTable.saveGMTimes(userId, gamemodeTimes);
    }

    /**
     *
     */
    @Override
    public void clean() {
        try {
            checkConnection();
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean removeAllData() {
        for (Table table : getAllTablesInRemoveOrder()) {
            if (!table.removeAllData()) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return
     */
    public boolean supportsModification() {
        return supportsModification;
    }

    /**
     *
     * @return
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     *
     * @return
     */
    public UsersTable getUsersTable() {
        return usersTable;
    }

    /**
     *
     * @return
     */
    public SessionsTable getSessionsTable() {
        return sessionsTable;
    }

    /**
     *
     * @return
     */
    public GMTimesTable getGmTimesTable() {
        return gmTimesTable;
    }

    /**
     *
     * @return
     */
    public KillsTable getKillsTable() {
        return killsTable;
    }

    /**
     *
     * @return
     */
    public LocationsTable getLocationsTable() {
        return locationsTable;
    }

    /**
     *
     * @return
     */
    public IPsTable getIpsTable() {
        return ipsTable;
    }

    /**
     *
     * @return
     */
    public NicknamesTable getNicknamesTable() {
        return nicknamesTable;
    }

    /**
     *
     * @return
     */
    public CommandUseTable getCommandUseTable() {
        return commandUseTable;
    }
}
