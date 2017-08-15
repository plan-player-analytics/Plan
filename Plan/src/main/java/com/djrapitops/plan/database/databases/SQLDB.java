package main.java.com.djrapitops.plan.database.databases;

import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.*;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class containing main logic for different data related save & load functionality.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public abstract class SQLDB extends Database {

    private final boolean supportsModification;
    private final boolean usingMySQL;

    private Connection connection;

    /**
     * @param plugin
     * @param supportsModification
     */
    public SQLDB(Plan plugin, boolean supportsModification) {
        super(plugin);
        this.supportsModification = supportsModification;
        usingMySQL = getName().equals("MySQL");

        usersTable = new UsersTable(this, usingMySQL);
        gmTimesTable = new GMTimesTable(this, usingMySQL);
        sessionsTable = new SessionsTable(this, usingMySQL);
        killsTable = new KillsTable(this, usingMySQL);
        locationsTable = new LocationsTable(this, usingMySQL);
        ipsTable = new IPsTable(this, usingMySQL);
        nicknamesTable = new NicknamesTable(this, usingMySQL);
        commandUseTable = new CommandUseTable(this, usingMySQL);
        versionTable = new VersionTable(this, usingMySQL);
        tpsTable = new TPSTable(this, usingMySQL);
        securityTable = new SecurityTable(this, usingMySQL);
        worldTable = new WorldTable(this, usingMySQL);
        worldTimesTable = new WorldTimesTable(this, usingMySQL);

        startConnectionPingTask();
    }

    /**
     * Starts repeating Async task that maintains the Database connection.
     */
    public void startConnectionPingTask() {
        // Maintains Connection.
        plugin.getRunnableFactory().createNew(new AbsRunnable("DBConnectionPingTask " + getName()) {
            @Override
            public void run() {
                Statement statement = null;
                try {
                    if (connection != null && !connection.isClosed()) {
                        statement = connection.createStatement();
                        statement.execute("/* ping */ SELECT 1");
                    }
                } catch (SQLException e) {
                    connection = getNewConnection();
                } finally {
                    MiscUtils.close(statement);
                }
            }
        }).runTaskTimerAsynchronously(60L * 20L, 60L * 20L);
    }

    /**
     * Initializes the Database.
     * <p>
     * All tables exist in the database after call to this.
     * Updates Schema to latest version.
     * Converts Unsaved Bukkit player files to database data.
     * Cleans the database.
     *
     * @return Was the Initialization successful.
     */
    @Override
    public boolean init() {
        super.init();
        setStatus("Init");
        String benchName = "Init " + getConfigName();
        Benchmark.start(benchName);
        try {
            if (!checkConnection()) {
                return false;
            }
            convertBukkitDataToDB();
            clean();
            return true;
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
            return false;
        } finally {
            Benchmark.stop("Database", benchName);
            Log.logDebug("Database");
        }
    }

    /**
     * Ensures connection functions correctly and all tables exist.
     * <p>
     * Updates to latest schema.
     *
     * @return Is the connection usable?
     * @throws SQLException
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
            } catch (Exception ignored) {

            }
            if (!versionTable.createTable()) {
                Log.error("Failed to create table: " + versionTable.getTableName());
                return false;
            }

            if (newDatabase) {
                Log.info("New Database created.");
                setVersion(8);
            }

            Benchmark.start("DCreate tables");

            for (Table table : getAllTables()) {
                if (!table.createTable()) {
                    Log.error("Failed to create table: " + table.getTableName());
                    return false;
                }
            }

            if (!securityTable.createTable()) {
                Log.error("Failed to create table: " + securityTable.getTableName());
                return false;
            }
            Benchmark.stop("Database", "Create tables");

            if (!newDatabase && getVersion() < 8) {
                setVersion(8);
            }
        }
        return true;
    }

    /**
     *
     */
    public void convertBukkitDataToDB() {
        plugin.getRunnableFactory().createNew(new AbsRunnable("BukkitDataConversionTask") {
            @Override
            public void run() {
                try {
                    Benchmark.start("Convert BukkitData to DB data");
                    Log.debug("Database", "Bukkit Data Conversion");
                    Set<UUID> uuids = usersTable.getSavedUUIDs();
                    uuids.removeAll(usersTable.getContainsBukkitData(uuids));
                    if (uuids.isEmpty()) {
                        Log.debug("Database", "No conversion necessary.");
                        return;
                    }
                    setStatus("Bukkit Data Conversion");
                    Log.info("Beginning Bukkit Data -> DB Conversion for " + uuids.size() + " players");
                    int id = plugin.getBootAnalysisTaskID();
                    if (id != -1) {
                        Log.info("Analysis | Cancelled Boot Analysis Due to conversion.");
                        plugin.getServer().getScheduler().cancelTask(id);
                    }
                    saveMultipleUserData(getUserDataForUUIDS(uuids));
                    Log.info("Conversion complete, took: " + FormatUtils.formatTimeAmount(Benchmark.stop("Database", "Convert BukkitData to DB data")) + " ms");
                } catch (SQLException ex) {
                    Log.toLog(this.getClass().getName(), ex);
                } finally {
                    setAvailable();
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }

    /**
     * @return
     */
    public Table[] getAllTables() {
        return new Table[]{
                usersTable, gmTimesTable, ipsTable,
                nicknamesTable, sessionsTable, killsTable,
                commandUseTable, tpsTable, worldTable,
                worldTimesTable};
    }

    /**
     * @return
     */
    public Table[] getAllTablesInRemoveOrder() {
        return new Table[]{
                locationsTable, gmTimesTable, ipsTable,
                nicknamesTable, sessionsTable, killsTable,
                worldTimesTable, worldTable, usersTable,
                commandUseTable, tpsTable};
    }

    /**
     * @return
     */
    public abstract Connection getNewConnection();

    /**
     * @throws SQLException
     */
    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        Log.logDebug("Database"); // Log remaining Debug info if present
        setStatus("Closed");
    }

    /**
     * @return @throws SQLException
     */
    @Override
    public int getVersion() throws SQLException {
        return versionTable.getVersion();
    }

    /**
     * @param version
     * @throws SQLException
     */
    @Override
    public void setVersion(int version) throws SQLException {
        versionTable.setVersion(version);
        commit();
    }

    /**
     * @param uuid
     * @return
     */
    @Override
    public boolean wasSeenBefore(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        setStatus("User exist check");
        try {
            return usersTable.getUserId(uuid.toString()) != -1;
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
            return false;
        } finally {
            setAvailable();
        }
    }

    /**
     * @param uuid
     * @return
     * @throws SQLException
     */
    @Override
    public boolean removeAccount(String uuid) throws SQLException {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }
        try {
            setStatus("Remove account " + uuid);
            Benchmark.start("Remove Account");
            Log.debug("Database", "Removing Account: " + uuid);
            try {
                checkConnection();
            } catch (Exception e) {
                Log.toLog(this.getClass().getName(), e);
                return false;
            }
            int userId = usersTable.getUserId(uuid);
            boolean success = userId != -1
                    && locationsTable.removeUserLocations(userId)
                    && ipsTable.removeUserIPs(userId)
                    && nicknamesTable.removeUserNicknames(userId)
                    && gmTimesTable.removeUserGMTimes(userId)
                    && sessionsTable.removeUserSessions(userId)
                    && killsTable.removeUserKillsAndVictims(userId)
                    && worldTimesTable.removeUserWorldTimes(userId)
                    && usersTable.removeUser(uuid);
            if (success) {
                commit();
            } else {
                rollback();
            }
            return success;
        } finally {
            Benchmark.stop("Database", "Remove Account");
            setAvailable();
        }
    }

    /**
     * @param uuid
     * @param processors
     * @throws SQLException
     */
    @Override
    public void giveUserDataToProcessors(UUID uuid, Collection<DBCallableProcessor> processors) throws SQLException {
        Benchmark.start("Give userdata to processors");
        try {
            checkConnection();
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            return;
        }
        // Check if user is in the database
        if (!wasSeenBefore(uuid)) {
            return;
        }
        setStatus("Get single userdata for " + uuid);
        // Get the data
        UserData data = usersTable.getUserData(uuid);

        int userId = usersTable.getUserId(uuid);

        List<String> nicknames = nicknamesTable.getNicknames(userId);
        data.addNicknames(nicknames);
        if (!nicknames.isEmpty()) {
            data.setLastNick(nicknames.get(nicknames.size() - 1));
        }

        List<InetAddress> ips = ipsTable.getIPAddresses(userId);
        data.addIpAddresses(ips);

        Map<String, Long> gmTimes = gmTimesTable.getGMTimes(userId);
        data.getGmTimes().setTimes(gmTimes);
        Map<String, Long> worldTimes = worldTimesTable.getWorldTimes(userId);
        WorldTimes worldT = data.getWorldTimes();
        worldT.setTimes(worldTimes);
        if (worldT.getLastStateChange() == 0) {
            worldT.setLastStateChange(data.getPlayTime());
        }

        List<SessionData> sessions = sessionsTable.getSessionData(userId);
        data.addSessions(sessions);
        data.setPlayerKills(killsTable.getPlayerKills(userId));
        processors.forEach(processor -> processor.process(data));
        Benchmark.stop("Database", "Give userdata to processors");
        setAvailable();
    }

    /**
     * @param uuidsCol
     * @return
     * @throws SQLException
     */
    @Override
    public List<UserData> getUserDataForUUIDS(Collection<UUID> uuidsCol) throws SQLException {
        if (uuidsCol == null || uuidsCol.isEmpty()) {
            return new ArrayList<>();
        }
        setStatus("Get userdata (multiple) for: " + uuidsCol.size());
        Benchmark.start("Get UserData for " + uuidsCol.size());
        Map<UUID, Integer> userIds = usersTable.getAllUserIds();
        Set<UUID> remove = uuidsCol.stream()
                .filter(uuid -> !userIds.containsKey(uuid))
                .collect(Collectors.toSet());
        List<UUID> uuids = new ArrayList<>(uuidsCol);
        Log.debug("Database", "Data not found for: " + remove.size());
        uuids.removeAll(remove);
        Benchmark.start("Create UserData objects for " + userIds.size());
        List<UserData> data = usersTable.getUserData(new ArrayList<>(uuids));
        Benchmark.stop("Database", "Create UserData objects for " + userIds.size());
        if (data.isEmpty()) {
            return data;
        }
        Map<Integer, UUID> idUuidRel = userIds.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        List<Integer> ids = userIds.entrySet().stream().filter(e -> uuids.contains(e.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
        Log.debug("Database", "Using IDs: " + ids.size());
        Map<Integer, List<String>> nicknames = nicknamesTable.getNicknames(ids);
        Map<Integer, Set<InetAddress>> ipList = ipsTable.getIPList(ids);
        Map<Integer, List<KillData>> playerKills = killsTable.getPlayerKills(ids, idUuidRel);
        Map<Integer, List<SessionData>> sessionData = sessionsTable.getSessionData(ids);
        Map<Integer, Map<String, Long>> gmTimes = gmTimesTable.getGMTimes(ids);
        Map<Integer, Map<String, Long>> worldTimes = worldTimesTable.getWorldTimes(ids);

        Log.debug("Database", "Data found for:");
        Log.debug("Database", "  UUIDs: " + uuids.size());
        Log.debug("Database", "  IDs: " + userIds.size());
        Log.debug("Database", "  UserData: " + data.size());
        Log.debug("Database", "    Nicknames: " + nicknames.size());
        Log.debug("Database", "    IPs: " + ipList.size());
        Log.debug("Database", "    Kills: " + playerKills.size());
        Log.debug("Database", "    Sessions: " + sessionData.size());
        Log.debug("Database", "    GM Times: " + gmTimes.size());
        Log.debug("Database", "    World Times: " + worldTimes.size());

        for (UserData uData : data) {
            UUID uuid = uData.getUuid();
            Integer id = userIds.get(uuid);
            uData.addIpAddresses(ipList.get(id));
            List<String> userNicks = nicknames.get(id);
            uData.addNicknames(userNicks);
            if (!userNicks.isEmpty()) {
                uData.setLastNick(userNicks.get(userNicks.size() - 1));
            }
            uData.addSessions(sessionData.get(id));
            uData.setPlayerKills(playerKills.get(id));
            uData.getGmTimes().setTimes(gmTimes.get(id));
            WorldTimes worldT = uData.getWorldTimes();
            worldT.setTimes(worldTimes.get(id));
            if (worldT.getLastStateChange() == 0) {
                worldT.setLastStateChange(uData.getPlayTime());
            }
        }

        Benchmark.stop("Database", "Get UserData for " + uuidsCol.size());
        setAvailable();
        return data;
    }

    /**
     * @param data
     * @throws SQLException
     */
    @Override
    public void saveMultipleUserData(Collection<UserData> data) throws SQLException {
        if (data == null || data.isEmpty()) {
            return;
        }

        Benchmark.start("Save multiple Userdata");
        data.removeIf(Objects::isNull);

        checkConnection();
        setStatus("Save userdata (multiple) for " + data.size());
        usersTable.saveUserDataInformationBatch(data);

        // Transform to map
        Map<UUID, UserData> userDatas = data.stream()
                .collect(Collectors.toMap(UserData::getUuid, Function.identity()));

        // Get UserIDs
        Map<UUID, Integer> userIds = usersTable.getAllUserIds();
        // Create empty data sets
        Map<Integer, Set<String>> nicknames = new HashMap<>();
        Map<Integer, String> lastNicks = new HashMap<>();
        Map<Integer, Set<InetAddress>> ips = new HashMap<>();
        Map<Integer, List<KillData>> kills = new HashMap<>();
        Map<Integer, UUID> uuids = userIds.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        Map<Integer, List<SessionData>> sessions = new HashMap<>();
        Map<Integer, Map<String, Long>> gmTimes = new HashMap<>();
        Map<Integer, Map<String, Long>> worldTimes = new HashMap<>();

        // Put in data set
        List<String> worldNames = data.stream()
                .map(UserData::getWorldTimes)
                .map(WorldTimes::getTimes)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        for (Map.Entry<UUID, UserData> entrySet : userDatas.entrySet()) {
            UUID uuid = entrySet.getKey();
            UserData uData = entrySet.getValue();
            Integer id = userIds.get(uuid);

            if (id == -1) {
                Log.debug("Database", "User not seen before, saving last: " + uuid);
                continue;
            }

            uData.access();
            nicknames.put(id, new HashSet<>(uData.getNicknames()));
            lastNicks.put(id, uData.getLastNick());
            ips.put(id, new HashSet<>(uData.getIps()));
            kills.put(id, new ArrayList<>(uData.getPlayerKills()));
            sessions.put(id, new ArrayList<>(uData.getSessions()));
            gmTimes.put(id, new HashMap<>(uData.getGmTimes().getTimes()));
            worldTimes.put(id, new HashMap<>(uData.getWorldTimes().getTimes()));
        }

        // Save
        nicknamesTable.saveNickLists(nicknames, lastNicks);
        ipsTable.saveIPList(ips);
        killsTable.savePlayerKills(kills, uuids);
        sessionsTable.saveSessionData(sessions);
        gmTimesTable.saveGMTimes(gmTimes);
        worldTable.saveWorlds(worldNames);
        worldTimesTable.saveWorldTimes(worldTimes);
        commit();
        userDatas.values().stream()
                .filter(Objects::nonNull)
                .filter(UserData::isAccessed)
                .forEach(UserData::stopAccessing);
        Benchmark.stop("Database", "Save multiple Userdata");
        setAvailable();
    }

    /**
     * @param data
     * @throws SQLException
     */
    @Override
    public void saveUserData(UserData data) throws SQLException {
        if (data == null) {
            return;
        }
        UUID uuid = data.getUuid();
        if (uuid == null) {
            return;
        }
        Log.debug("Database", "Save userdata: " + uuid);
        Benchmark.start("Save Single UserData");
        checkConnection();
        data.access();
        usersTable.saveUserDataInformation(data);
        int userId = usersTable.getUserId(uuid.toString());
        sessionsTable.saveSessionData(userId, new ArrayList<>(data.getSessions()));
        nicknamesTable.saveNickList(userId, new HashSet<>(data.getNicknames()), data.getLastNick());
        ipsTable.saveIPList(userId, new HashSet<>(data.getIps()));
        killsTable.savePlayerKills(userId, new ArrayList<>(data.getPlayerKills()));
        gmTimesTable.saveGMTimes(userId, data.getGmTimes().getTimes());
        worldTable.saveWorlds(new HashSet<>(data.getWorldTimes().getTimes().keySet()));
        worldTimesTable.saveWorldTimes(userId, data.getWorldTimes().getTimes());
        data.stopAccessing();
        commit();
        Benchmark.stop("Database", "Save Single UserData");
        setAvailable();
    }

    /**
     *
     */
    @Override
    public void clean() {
        Log.info("Cleaning the database.");
        try {
            checkConnection();
            tpsTable.clean();
            locationsTable.removeAllData();
            Log.info("Clean complete.");
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    /**
     * @return
     */
    @Override
    public boolean removeAllData() {
        boolean success = true;
        setStatus("Clearing all data");
        try {
            for (Table table : getAllTablesInRemoveOrder()) {
                if (!table.removeAllData()) {
                    success = false;
                    break;
                }
            }
            if (success) {
                commit();
            } else {
                rollback(); // TODO Tests for this case
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        setAvailable();
        return success;
    }

    /**
     * @return
     */
    public boolean supportsModification() {
        return supportsModification;
    }

    /**
     * @return
     */
    public Connection getConnection() {
        return connection;
    }

    private void setStatus(String status) {
        plugin.processStatus().setStatus("DB-" + getName(), status);
    }

    public void setAvailable() {
        setStatus("Running");
        Log.logDebug("Database");
    }

    /**
     * Commits changes to the .db file when using SQLite Database.
     * <p>
     * MySQL has Auto Commit enabled.
     */
    public void commit() throws SQLException {
        if (!usingMySQL) {
            getConnection().commit();
        }
    }

    /**
     * Reverts transaction when using SQLite Database.
     * <p>
     * MySQL has Auto Commit enabled.
     */
    public void rollback() throws SQLException {
        if (!usingMySQL) {
            connection.rollback();
        }
    }
}
