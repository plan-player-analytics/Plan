package main.java.com.djrapitops.plan.database.databases;

import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.ITask;
import com.djrapitops.plugin.task.RunnableFactory;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import main.java.com.djrapitops.plan.data.PlayerProfile;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.*;
import main.java.com.djrapitops.plan.database.tables.move.Version8TransferTable;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Class containing main logic for different data related save and load functionality.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public abstract class SQLDB extends Database {

    private final boolean usingMySQL;
    private boolean open = false;
    private ITask dbCleanTask;

    public SQLDB(IPlan plugin) {
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
    }

    /**
     * Initializes the Database.
     * <p>
     * All tables exist in the database after call to this.
     * Updates Schema to latest version.
     * Converts Unsaved Bukkit player files to database data.
     * Cleans the database.
     *
     * @throws DatabaseInitException if Database fails to initiate.
     */
    @Override
    public void init() throws DatabaseInitException {
        setStatus("Init");
        String benchName = "Init " + getConfigName();
        Benchmark.start("Database", benchName);
        try {
            setupDataSource();
            setupDatabase();
            scheduleClean(10L);
            open = true;
        } finally {
            Benchmark.stop("Database", benchName);
            Log.logDebug("Database");
        }
    }

    public void scheduleClean(long secondsDelay) {
        dbCleanTask = RunnableFactory.createNew("DB Clean Task", new AbsRunnable() {
            @Override
            public void run() {
                try {
                    if (isOpen()) {
                        clean();
                    }
                } catch (SQLException e) {
                    Log.toLog(this.getClass().getName(), e);
                } finally {
                    cancel();
                }
            }
        }).runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * secondsDelay);
    }

    /**
     * Ensures connection functions correctly and all tables exist.
     * <p>
     * Updates to latest schema.
     *
     * @throws DatabaseInitException if something goes wrong.
     */
    public void setupDatabase() throws DatabaseInitException {
        try {
            boolean newDatabase = isNewDatabase();

            versionTable.createTable();
            createTables();

            if (newDatabase) {
                Log.info("New Database created.");
                setVersion(13);
            }

            int version = getVersion();

            final SQLDB db = this;
            if (version < 10) {
                RunnableFactory.createNew("DB v8 -> v10 Task", new AbsRunnable() {
                    @Override
                    public void run() {
                        try {
                            new Version8TransferTable(db, isUsingMySQL()).alterTablesToV10();
                        } catch (DatabaseInitException | SQLException e) {
                            Log.toLog(this.getClass().getName(), e);
                        }
                    }
                }).runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * 5L);
            }
            if (version < 11) {
                serverTable.alterTableV11();
                setVersion(11);
            }
            if (version < 12) {
                actionsTable.alterTableV12();
                ipsTable.alterTableV12();
                setVersion(12);
            }
            if (version < 13) {
                ipsTable.alterTableV13();
                setVersion(13);
            }
        } catch (SQLException e) {
            throw new DatabaseInitException("Failed to set-up Database", e);
        }
    }

    /**
     * Creates the tables that contain data.
     * <p>
     * Updates table columns to latest schema.
     */
    private void createTables() throws DatabaseInitException {
        Benchmark.start("Database", "Create tables");
        for (Table table : getAllTables()) {
            table.createTable();
        }
        Benchmark.stop("Database", "Create tables");
    }

    /**
     * Get all tables in a create order.
     *
     * @return Table array.
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
     * Get all tables for removal of data.
     *
     * @return Tables in the order the data should be removed in.
     */
    public Table[] getAllTablesInRemoveOrder() {
        return new Table[]{
                ipsTable, nicknamesTable, killsTable,
                worldTimesTable, sessionsTable, actionsTable,
                worldTable, userInfoTable, usersTable,
                commandUseTable, tpsTable, securityTable,
                serverTable
        };
    }

    /**
     * Setups the {@link BasicDataSource}
     */
    public abstract void setupDataSource() throws DatabaseInitException;

    /**
     * Closes the SQLDB
     *
     * @throws SQLException DB Error
     */
    @Override
    public void close() throws SQLException {
        setStatus("Closed");
        open = false;
        Log.logDebug("Database"); // Log remaining Debug info if present
        if (dbCleanTask != null) {
            dbCleanTask.cancel();
        }
    }

    /**
     * @return @throws SQLException
     */
    @Override
    public int getVersion() throws SQLException {
        return versionTable.getVersion();
    }

    @Override
    public void setVersion(int version) throws SQLException {
        versionTable.setVersion(version);
    }

    @Override
    public boolean isNewDatabase() throws SQLException {
        return versionTable.isNewDatabase();
    }

    @Override
    public PlayerProfile getPlayerProfile(UUID uuid) throws SQLException {
        if (!wasSeenBefore(uuid)) {
            return null;
        }

        String playerName = usersTable.getPlayerName(uuid);
        Optional<Long> registerDate = usersTable.getRegisterDate(uuid);

        if (!registerDate.isPresent()) {
            throw new IllegalStateException("User has been saved with null register date to a NOT NULL column");
        }

        PlayerProfile profile = new PlayerProfile(uuid, playerName, registerDate.get());
        profile.setTimesKicked(usersTable.getTimesKicked(uuid));

        Map<UUID, UserInfo> userInfo = userInfoTable.getAllUserInfo(uuid);
        addUserInfoToProfile(profile, userInfo);

        profile.setActions(actionsTable.getActions(uuid));
        profile.setNicknames(nicknamesTable.getAllNicknames(uuid));
        profile.setGeoInformation(ipsTable.getGeoInfo(uuid));

        Map<UUID, List<Session>> sessions = sessionsTable.getSessions(uuid);
        profile.setSessions(sessions);
        profile.setTotalWorldTimes(worldTimesTable.getWorldTimesOfUser(uuid));

        return profile;
    }

    private void addUserInfoToProfile(PlayerProfile profile, Map<UUID, UserInfo> userInfo) {
        for (Map.Entry<UUID, UserInfo> entry : userInfo.entrySet()) {
            UUID serverUUID = entry.getKey();
            UserInfo info = entry.getValue();

            profile.setRegistered(serverUUID, info.getRegistered());
            if (info.isBanned()) {
                profile.bannedOnServer(serverUUID);
            }
            if (info.isOpped()) {
                profile.oppedOnServer(serverUUID);
            }
        }
    }

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

    public void removeAccount(UUID uuid) throws SQLException {
        if (uuid == null) {
            return;
        }

        try {
            Log.logDebug("Database", "Removing Account: " + uuid);
            Benchmark.start("Database", "Remove Account");

            for (Table t : getAllTablesInRemoveOrder()) {
                if (!(t instanceof UserIDTable)) {
                    continue;
                }

                UserIDTable table = (UserIDTable) t;
                table.removeUser(uuid);
            }
        } finally {
            Benchmark.stop("Database", "Remove Account");
            setAvailable();
        }
    }

    private void clean() throws SQLException {
        Log.info("Cleaning the database.");
        tpsTable.clean();
        Log.info("Clean complete.");
    }

    @Override
    public void removeAllData() throws SQLException {
        setStatus("Clearing all data");
        try {
            for (Table table : getAllTablesInRemoveOrder()) {
                table.removeAllData();
            }
        } finally {
            setAvailable();
        }
    }

    private void setStatus(String status) {
        Log.logDebug("Database", status);
    }

    public void setAvailable() {
        Log.logDebug("Database");
    }

    public abstract Connection getConnection() throws SQLException;

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
            returnToPool(connection);
        }
    }

    @Override
    public void returnToPool(Connection connection) throws SQLException {
        if (usingMySQL && connection != null) {
            connection.close();
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
            returnToPool(connection);
        }
    }

    public boolean isOpen() {
        return open;
    }
}
