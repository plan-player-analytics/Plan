package main.java.com.djrapitops.plan.database.databases;

import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.*;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Class containing main logic for different data related save & load functionality.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public abstract class SQLDB extends Database {

    private final boolean usingMySQL;

    private Connection connection;

    /**
     * @param plugin
     */
    public SQLDB(Plan plugin) {
        super(plugin);
        usingMySQL = getName().equals("MySQL");

        versionTable = new VersionTable(this, usingMySQL);
        serverTable = new ServerTable(this, usingMySQL);
        securityTable = new SecurityTable(this, usingMySQL);

        commandUseTable = new CommandUseTable(this, usingMySQL);
        tpsTable = new TPSTable(this, usingMySQL);

        usersTable = new UsersTable(this, usingMySQL);
        userInfoTable = new UserInfoTable(this, usingMySQL);
        actionsTable = new ActionsTable(this, usingMySQL);
        ipsTable = new IPsTable(this, usingMySQL);
        nicknamesTable = new NicknamesTable(this, usingMySQL);
        sessionsTable = new SessionsTable(this, usingMySQL);
        killsTable = new KillsTable(this, usingMySQL);
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
                serverTable, usersTable, userInfoTable, ipsTable,
                nicknamesTable, sessionsTable, killsTable,
                commandUseTable, actionsTable, tpsTable,
                worldTable, worldTimesTable, securityTable
        };
    }

    /**
     * Get all tables except securityTable for removal of user data.
     *
     * @return Tables in the order the data should be removed in.
     */
    public Table[] getAllTablesInRemoveOrder() {
        return new Table[]{
                ipsTable, nicknamesTable, killsTable,
                worldTimesTable, sessionsTable, actionsTable,
                worldTable, userInfoTable, usersTable,
                commandUseTable, tpsTable, serverTable
        };
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
            return usersTable.isRegistered(uuid);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
            return false;
        } finally {
            setAvailable();
        }
    }

    public boolean removeAccount(UUID uuid) throws SQLException {
        if (uuid == null) {
            return false;
        }
        try {
            Benchmark.start("Remove Account");
            Log.debug("Database", "Removing Account: " + uuid);
            checkConnection();

            boolean success = true;
            for (Table t : getAllTablesInRemoveOrder()) {
                if (!success) {
                    continue;
                }
                if (t instanceof UserIDTable) {
                    UserIDTable table = (UserIDTable) t;
                    success = table.removeUser(uuid);
                }
            }
            if (success) {
                commit();
                return true;
            }
            throw new IllegalStateException("Removal Failed");
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            rollback(); // TODO Test case
            return false;
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
    public List<UserInfo> getUserDataForUUIDS(Collection<UUID> uuidsCol) throws SQLException {
        if (uuidsCol == null || uuidsCol.isEmpty()) {
            return new ArrayList<>();
        }
        // TODO REWRITE
        return new ArrayList<>();
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
