package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.*;
import com.djrapitops.plan.system.database.databases.sql.operation.*;
import com.djrapitops.plan.system.database.databases.sql.tables.*;
import com.djrapitops.plan.system.database.databases.sql.tables.move.Version8TransferTable;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.ITask;
import com.djrapitops.plugin.task.RunnableFactory;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Class containing main logic for different data related save and load functionality.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public abstract class SQLDB extends Database {

    private final UsersTable usersTable;
    private final UserInfoTable userInfoTable;
    private final ActionsTable actionsTable;
    private final KillsTable killsTable;
    private final NicknamesTable nicknamesTable;
    private final SessionsTable sessionsTable;
    private final GeoInfoTable geoInfoTable;
    private final CommandUseTable commandUseTable;
    private final TPSTable tpsTable;
    private final VersionTable versionTable;
    private final SecurityTable securityTable;
    private final WorldTable worldTable;
    private final WorldTimesTable worldTimesTable;
    private final ServerTable serverTable;
    private final TransferTable transferTable;

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

    public SQLDB() {
        usingMySQL = getName().equals("MySQL");

        versionTable = new VersionTable(this);
        serverTable = new ServerTable(this);
        securityTable = new SecurityTable(this);

        commandUseTable = new CommandUseTable(this);
        tpsTable = new TPSTable(this);

        usersTable = new UsersTable(this);
        userInfoTable = new UserInfoTable(this);
        actionsTable = new ActionsTable(this);
        geoInfoTable = new GeoInfoTable(this);
        nicknamesTable = new NicknamesTable(this);
        sessionsTable = new SessionsTable(this);
        killsTable = new KillsTable(this);
        worldTable = new WorldTable(this);
        worldTimesTable = new WorldTimesTable(this);
        transferTable = new TransferTable(this);

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
                } catch (SQLException e) {
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
            boolean newDatabase = versionTable.isNewDatabase();

            versionTable.createTable();
            createTables();

            if (newDatabase) {
                Log.info("New Database created.");
                versionTable.setVersion(17);
            }

            int version = versionTable.getVersion();

            final SQLDB db = this;
            if (version < 10) {
                RunnableFactory.createNew("DB v8 -> v10 Task", new AbsRunnable() {
                    @Override
                    public void run() {
                        try {
                            new Version8TransferTable(db).alterTablesToV10();
                        } catch (DBInitException | SQLException e) {
                            Log.toLog(this.getClass(), e);
                        }
                    }
                }).runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * 5L);
            }
            if (version < 11) {
                serverTable.alterTableV11();
                versionTable.setVersion(11);
            }
            if (version < 12) {
                actionsTable.alterTableV12();
                geoInfoTable.alterTableV12();
                versionTable.setVersion(12);
            }
            if (version < 13) {
                geoInfoTable.alterTableV13();
                versionTable.setVersion(13);
            }
            if (version < 14) {
                transferTable.alterTableV14();
                versionTable.setVersion(14);
            }
            if (version < 15) {
                sessionsTable.alterTableV15();
                versionTable.setVersion(15);
            }
            if (version < 16) {
                killsTable.alterTableV16();
                worldTimesTable.alterTableV16();
                versionTable.setVersion(16);
            }
            if (version < 17) {
                geoInfoTable.alterTableV17();
                versionTable.setVersion(17);
            }
        } catch (SQLException e) {
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
                nicknamesTable, sessionsTable, killsTable,
                commandUseTable, actionsTable, tpsTable,
                worldTable, worldTimesTable, securityTable, transferTable
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
                worldTimesTable, sessionsTable, actionsTable,
                worldTable, userInfoTable, usersTable,
                commandUseTable, tpsTable, securityTable,
                serverTable
        };
    }

    /**
     * Setups the {@link BasicDataSource}
     */
    public abstract void setupDataSource() throws DBInitException;

    @Override
    public void close() {
        open = false;
        if (dbCleanTask != null) {
            dbCleanTask.cancel();
        }
    }

    public int getVersion() throws SQLException {
        return versionTable.getVersion();
    }

    public void setVersion(int version) throws SQLException {
        versionTable.setVersion(version);
    }

    private void clean() throws SQLException {
        tpsTable.clean();
        transferTable.clean();
    }

    public abstract Connection getConnection() throws SQLException;

    /**
     * Commits changes to the .db file when using SQLite Database.
     * <p>
     * MySQL has Auto Commit enabled.
     */
    public void commit(Connection connection) throws SQLException {
        try {
            if (!usingMySQL) {
                connection.commit();
            }
        } catch (SQLException e) {
            if (!e.getMessage().contains("cannot commit")) {
                Log.toLog(this.getClass(), e);
            }
        } finally {
            returnToPool(connection);
        }
    }

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

    public ActionsTable getActionsTable() {
        return actionsTable;
    }

    public UserInfoTable getUserInfoTable() {
        return userInfoTable;
    }

    public TransferTable getTransferTable() {
        return transferTable;
    }

    public boolean isUsingMySQL() {
        return this instanceof MySQLDB;
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
}
