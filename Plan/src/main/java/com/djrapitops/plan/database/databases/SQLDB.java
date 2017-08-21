package main.java.com.djrapitops.plan.database.databases;

import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.*;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.UUID;

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
