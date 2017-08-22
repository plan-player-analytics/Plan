package main.java.com.djrapitops.plan.database.databases;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.*;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import org.apache.commons.dbcp2.BasicDataSource;

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

    /**
     * @param plugin
     * @param supportsModification
     */
    public SQLDB(Plan plugin, boolean supportsModification) {
        super(plugin);
        this.supportsModification = supportsModification;
        usingMySQL = getName().equals("MySQL");

        serverTable = new ServerTable(this, usingMySQL);
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

        setupDataSource();
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
            if (!setupDatabases()) {
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
    public boolean setupDatabases() throws SQLException {
        boolean newDatabase = isNewDatabase();

        if (!versionTable.createTable()) {
            Log.error("Failed to create table: " + versionTable.getTableName());
            return false;
        }

        if (newDatabase) {
            Log.info("New Database created.");
        }

        if (!createTables()) {
            return false;
        }

        if (newDatabase || getVersion() < 8) {
            setVersion(8);
        }

        try (Statement statement = getConnection().createStatement()) {
            statement.execute("DROP TABLE IF EXISTS plan_locations");
            endTransaction(statement.getConnection());
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
                serverTable, usersTable, ipsTable,
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
                commandUseTable, tpsTable, serverTable};
    }

    /**
     * Setups the {@link BasicDataSource}
     */
    public abstract void setupDataSource();

    /**
     * @throws SQLException
     */
    @Override
    public void close() throws SQLException {
        /*if (!dataSource.isClosed()) {
            dataSource.close();
        }*/

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
                setupDatabases();
            } catch (Exception e) {
                Log.toLog(this.getClass().getName(), e);
                return false;
            }
            int userId = usersTable.getUserId(uuid);
            return userId != -1
                    && ipsTable.removeUserIPs(userId)
                    && nicknamesTable.removeUserNicknames(userId)
                    && sessionsTable.removeUserSessions(userId)
                    && killsTable.removeUserKillsAndVictims(userId)
                    && worldTimesTable.removeUserWorldTimes(userId)
                    && usersTable.removeUser(uuid);
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
            setupDatabases();
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
        try {
            setStatus("Clearing all data");

            for (Table table : getAllTablesInRemoveOrder()) {
                if (!table.removeAllData()) {
                    return false;
                }
            }

            return true;
        } finally {
            setAvailable();
        }
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

        // TODO REWRITE

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

    private void setStatus(String status) {
        Log.debug("Database", status);
    }

    public void setAvailable() {
        Log.logDebug("Database");
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Commits changes to the .db file when using SQLite Database.
     * <p>
     * MySQL has Auto Commit enabled.
     */
    @Override
    public void commit(Connection connection) throws SQLException {
        try {
            if (!usingMySQL) {
                connection.commit();
            }
        } finally {
            endTransaction(connection);
        }
    }

    /**
     * Reverts transaction when using SQLite Database.
     * <p>
     * MySQL has Auto Commit enabled.
     */
    public void rollback(Connection connection) throws SQLException {
        try {
            if (!usingMySQL) {
                connection.rollback();
            }
        } finally {
            endTransaction(connection);
        }
    }

    public void endTransaction(Connection connection) throws SQLException {
        connection.close();
    }
}
