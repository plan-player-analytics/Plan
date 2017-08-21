package main.java.com.djrapitops.plan.database.databases;

import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.*;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
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
        sessionsTable = new SessionsTable(this, usingMySQL);
        killsTable = new KillsTable(this, usingMySQL);
        ipsTable = new IPsTable(this, usingMySQL);
        nicknamesTable = new NicknamesTable(this, usingMySQL);
        commandUseTable = new CommandUseTable(this, usingMySQL);
        versionTable = new VersionTable(this, usingMySQL);
        tpsTable = new TPSTable(this, usingMySQL);
        securityTable = new SecurityTable(this, usingMySQL);
        worldTable = new WorldTable(this, usingMySQL);
        worldTimesTable = new WorldTimesTable(this, usingMySQL);
        serverTable = new ServerTable(this, usingMySQL);

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

            boolean newDatabase = isNewDatabase();

            if (!versionTable.createTable()) {
                Log.error("Failed to create table: " + versionTable.getTableName());
                return false;
            }

            if (newDatabase) {
                Log.info("New Database created.");
                setVersion(8);
            }

            if (!createTables()) {
                return false;
            }

            if (!newDatabase && getVersion() < 8) {
                setVersion(8);
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS plan_locations");
            }
        }
        return true;
    }

    /**
     * Creates the tables that contain data.
     * <p>
     * Updates table columns to latest schema.
     *
     * @return true if successful.
     */
    private boolean createTables() {
        Benchmark.start("Create tables");
        for (Table table : getAllTables()) {
            if (!table.createTable()) {
                Log.error("Failed to create table: " + table.getTableName());
                return false;
            }
        }
        Benchmark.stop("Database", "Create tables");
        return true;
    }

    private boolean isNewDatabase() {
        try {
            getVersion();
            return false;
        } catch (Exception ignored) {
            return true;
        }
    }

    /**
     * @return
     */
    public Table[] getAllTables() {
        return new Table[]{
                usersTable, ipsTable,
                nicknamesTable, sessionsTable, killsTable,
                commandUseTable, tpsTable, worldTable,
                worldTimesTable, securityTable};
    }

    /**
     * @return
     */
    public Table[] getAllTablesInRemoveOrder() {
        return new Table[]{
                ipsTable,
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
        setStatus("Closed");
        Log.logDebug("Database"); // Log remaining Debug info if present
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
                    && ipsTable.removeUserIPs(userId)
                    && nicknamesTable.removeUserNicknames(userId)
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
     *
     */
    @Override
    public void clean() {
        Log.info("Cleaning the database.");
        try {
            checkConnection();
            tpsTable.clean();
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
        List<UserData> data = usersTable.getUserData(uuids);
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
        Map<Integer, Map<String, Long>> worldTimes = worldTimesTable.getWorldTimes(ids);

        Log.debug("Database",
                "Data found for:",
                "  UUIDs: " + uuids.size(),
                "  IDs: " + userIds.size(),
                "  UserData: " + data.size(),
                "    Nicknames: " + nicknames.size(),
                "    IPs: " + ipList.size(),
                "    Kills: " + playerKills.size(),
                "    Sessions: " + sessionData.size(),
                "    World Times: " + worldTimes.size()
        );

        for (UserData uData : data) {
            // TODO add extra data
        }

        Benchmark.stop("Database", "Get UserData for " + uuidsCol.size());
        setAvailable();
        return data;
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
        Log.debug("Database", status);
    }

    public void setAvailable() {
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
