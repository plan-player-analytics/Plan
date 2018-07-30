package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.*;
import com.djrapitops.plan.system.database.databases.sql.operation.*;
import com.djrapitops.plan.system.database.databases.sql.patches.*;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.tables.*;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.ITask;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Class containing main logic for different data related save and load functionality.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public abstract class SQLDB extends Database {

    protected final Supplier<Locale> locale;

    private final UsersTable usersTable;
    private final UserInfoTable userInfoTable;
    private final KillsTable killsTable;
    private final NicknamesTable nicknamesTable;
    private final SessionsTable sessionsTable;
    private final GeoInfoTable geoInfoTable;
    private final CommandUseTable commandUseTable;
    private final TPSTable tpsTable;
    private final SecurityTable securityTable;
    private final WorldTable worldTable;
    private final WorldTimesTable worldTimesTable;
    private final ServerTable serverTable;
    private final TransferTable transferTable;
    private final PingTable pingTable;

    private final SQLBackupOps backupOps;
    private final SQLCheckOps checkOps;
    private final SQLFetchOps fetchOps;
    private final SQLRemoveOps removeOps;
    private final SQLSearchOps searchOps;
    private final SQLCountOps countOps;
    private final SQLSaveOps saveOps;
    private final SQLTransferOps transferOps;

    private final boolean usingMySQL;
    private ITask dbCleanTask;

    public SQLDB(Supplier<Locale> locale) {
        this.locale = locale;
        usingMySQL = this instanceof MySQLDB;

        serverTable = new ServerTable(this);
        securityTable = new SecurityTable(this);

        commandUseTable = new CommandUseTable(this);
        tpsTable = new TPSTable(this);

        usersTable = new UsersTable(this);
        userInfoTable = new UserInfoTable(this);
        geoInfoTable = new GeoInfoTable(this);
        nicknamesTable = new NicknamesTable(this);
        sessionsTable = new SessionsTable(this);
        killsTable = new KillsTable(this);
        worldTable = new WorldTable(this);
        worldTimesTable = new WorldTimesTable(this);
        transferTable = new TransferTable(this);
        pingTable = new PingTable(this);

        backupOps = new SQLBackupOps(this);
        checkOps = new SQLCheckOps(this);
        fetchOps = new SQLFetchOps(this);
        removeOps = new SQLRemoveOps(this);
        countOps = new SQLCountOps(this);
        searchOps = new SQLSearchOps(this);
        saveOps = new SQLSaveOps(this);
        transferOps = new SQLTransferOps(this);
    }

    /**
     * Initializes the Database.
     * <p>
     * All tables exist in the database after call to this.
     * Updates Schema to latest version.
     * Converts Unsaved Bukkit player files to database data.
     * Cleans the database.
     *
     * @throws DBInitException if Database fails to initiate.
     */
    @Override
    public void init() throws DBInitException {
        open = true;
        setupDataSource();
        setupDatabase();
    }

    @Override
    public void scheduleClean(long secondsDelay) {
        dbCleanTask = RunnableFactory.createNew("DB Clean Task", new AbsRunnable() {
            @Override
            public void run() {
                try {
                    if (isOpen()) {
                        clean();
                    }
                } catch (DBOpException e) {
                    Log.toLog(this.getClass(), e);
                    cancel();
                }
            }
        }).runTaskTimerAsynchronously(TimeAmount.SECOND.ticks() * secondsDelay, TimeAmount.MINUTE.ticks() * 5L);
    }

    /**
     * Ensures connection functions correctly and all tables exist.
     * <p>
     * Updates to latest schema.
     *
     * @throws DBInitException if something goes wrong.
     */
    public void setupDatabase() throws DBInitException {
        try {
            createTables();

            Patch[] patches = new Patch[]{
                    new Version10Patch(this),
                    new GeoInfoLastUsedPatch(this),
                    new TransferPartitionPatch(this),
                    new SessionAFKTimePatch(this),
                    new KillsServerIDPatch(this),
                    new WorldTimesSeverIDPatch(this),
                    new WorldsServerIDPatch(this),
                    new IPHashPatch(this),
                    new IPAnonPatch(this),
                    new NicknameLastSeenPatch(this),
                    new VersionTableRemovalPatch(this)
            };

            RunnableFactory.createNew("Database Patch", new AbsRunnable() {
                @Override
                public void run() {
                    try {
                        boolean applied = false;
                        for (Patch patch : patches) {
                            if (!patch.hasBeenApplied()) {
                                String patchName = patch.getClass().getSimpleName();
                                Log.info(locale.get().getString(PluginLang.DB_APPLY_PATCH, patchName));
                                patch.apply();
                                applied = true;
                            }
                        }
                        Log.info(locale.get().getString(
                                applied ? PluginLang.DB_APPLIED_PATCHES : PluginLang.DB_APPLIED_PATCHES_ALREADY
                        ));
                    } catch (Exception e) {
                        Log.error("----------------------------------------------------");
                        Log.error(locale.get().getString(PluginLang.ENABLE_FAIL_DB_PATCH));
                        Log.error("----------------------------------------------------");
                        Log.toLog(this.getClass(), e);
                        PlanPlugin.getInstance().onDisable();
                    }
                }
            }).runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * 5L);
        } catch (DBOpException e) {
            throw new DBInitException("Failed to set-up Database", e);
        }
    }

    /**
     * Creates the tables that contain data.
     * <p>
     * Updates table columns to latest schema.
     */
    private void createTables() throws DBInitException {
        for (Table table : getAllTables()) {
            table.createTable();
        }
    }

    /**
     * Get all tables in a create order.
     *
     * @return Table array.
     */
    public Table[] getAllTables() {
        return new Table[]{
                serverTable, usersTable, userInfoTable, geoInfoTable,
                nicknamesTable, sessionsTable, killsTable, pingTable,
                commandUseTable, tpsTable, worldTable,
                worldTimesTable, securityTable, transferTable
        };
    }

    /**
     * Get all tables for removal of data.
     *
     * @return Tables in the order the data should be removed in.
     */
    public Table[] getAllTablesInRemoveOrder() {
        return new Table[]{
                transferTable, geoInfoTable, nicknamesTable, killsTable,
                worldTimesTable, sessionsTable, worldTable, pingTable,
                userInfoTable, usersTable, commandUseTable,
                tpsTable, securityTable, serverTable
        };
    }

    public abstract void setupDataSource() throws DBInitException;

    @Override
    public void close() {
        open = false;
        if (dbCleanTask != null) {
            dbCleanTask.cancel();
        }
    }

    private void clean() {
        tpsTable.clean();
        transferTable.clean();
        geoInfoTable.clean();
        pingTable.clean();

        long now = System.currentTimeMillis();
        long keepActiveAfter = now - TimeAmount.DAY.ms() * Settings.KEEP_INACTIVE_PLAYERS_DAYS.getNumber();

        List<UUID> inactivePlayers = sessionsTable.getLastSeenForAllPlayers().entrySet().stream()
                .filter(entry -> entry.getValue() < keepActiveAfter)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        for (UUID uuid : inactivePlayers) {
            removeOps.player(uuid);
        }
        int removed = inactivePlayers.size();
        if (removed > 0) {
            Log.info(locale.get().getString(PluginLang.DB_NOTIFY_CLEAN, removed));
        }
    }

    public abstract Connection getConnection() throws SQLException;

    public abstract void commit(Connection connection);

    public abstract void returnToPool(Connection connection);

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

    public boolean execute(ExecStatement statement) {
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement.getSql())) {
                return statement.execute(preparedStatement);
            }
        } catch (SQLException e) {
            throw DBOpException.forCause(statement.getSql(), e);
        } finally {
            commit(connection);
        }
    }

    public boolean execute(String sql) {
        return execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) {

            }
        });
    }

    public void executeUnsafe(String... statements) {
        Verify.nullCheck(statements);
        for (String statement : statements) {
            try {
                execute(statement);
            } catch (DBOpException e) {
                if (Settings.DEV_MODE.isTrue()) {
                    Log.toLog(this.getClass(), e);
                }
            }
        }
    }

    public void executeBatch(ExecStatement statement) {
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement.getSql())) {
                statement.executeBatch(preparedStatement);
            }
        } catch (SQLException e) {
            throw DBOpException.forCause(statement.getSql(), e);
        } finally {
            commit(connection);
        }
    }

    public <T> T query(QueryStatement<T> statement) {
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement.getSql())) {
                return statement.executeQuery(preparedStatement);
            }
        } catch (SQLException e) {
            throw DBOpException.forCause(statement.getSql(), e);
        } finally {
            returnToPool(connection);
        }
    }

    public UsersTable getUsersTable() {
        return usersTable;
    }

    public SessionsTable getSessionsTable() {
        return sessionsTable;
    }

    public KillsTable getKillsTable() {
        return killsTable;
    }

    public GeoInfoTable getGeoInfoTable() {
        return geoInfoTable;
    }

    public NicknamesTable getNicknamesTable() {
        return nicknamesTable;
    }

    public CommandUseTable getCommandUseTable() {
        return commandUseTable;
    }

    public TPSTable getTpsTable() {
        return tpsTable;
    }

    public SecurityTable getSecurityTable() {
        return securityTable;
    }

    public WorldTable getWorldTable() {
        return worldTable;
    }

    public WorldTimesTable getWorldTimesTable() {
        return worldTimesTable;
    }

    public ServerTable getServerTable() {
        return serverTable;
    }

    public UserInfoTable getUserInfoTable() {
        return userInfoTable;
    }

    public TransferTable getTransferTable() {
        return transferTable;
    }

    public PingTable getPingTable() {
        return pingTable;
    }

    public boolean isUsingMySQL() {
        return usingMySQL;
    }

    @Override
    public BackupOperations backup() {
        return backupOps;
    }

    @Override
    public CheckOperations check() {
        return checkOps;
    }

    @Override
    public FetchOperations fetch() {
        return fetchOps;
    }

    @Override
    public RemoveOperations remove() {
        return removeOps;
    }

    @Override
    public SearchOperations search() {
        return searchOps;
    }

    @Override
    public CountOperations count() {
        return countOps;
    }

    @Override
    public SaveOperations save() {
        return saveOps;
    }

    @Override
    public TransferOperations transfer() {
        return transferOps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SQLDB sqldb = (SQLDB) o;
        return usingMySQL == sqldb.usingMySQL && getName().equals(sqldb.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(usingMySQL, getName());
    }
}
