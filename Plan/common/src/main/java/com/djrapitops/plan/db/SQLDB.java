/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.db;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.access.transactions.init.CleanTransaction;
import com.djrapitops.plan.db.access.transactions.init.CreateIndexTransaction;
import com.djrapitops.plan.db.access.transactions.init.CreateTablesTransaction;
import com.djrapitops.plan.db.patches.*;
import com.djrapitops.plan.db.sql.tables.TPSTable;
import com.djrapitops.plan.db.tasks.PatchTask;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.system.database.databases.sql.operation.SQLFetchOps;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.PluginSettings;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.PluginTask;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Class containing main logic for different data related save and load functionality.
 *
 * @author Rsl1122
 */
public abstract class SQLDB extends AbstractDatabase {

    private final Supplier<UUID> serverUUIDSupplier;

    protected final Locale locale;
    protected final PlanConfig config;
    protected final NetworkContainer.Factory networkContainerFactory;
    protected final RunnableFactory runnableFactory;
    protected final PluginLogger logger;
    protected final Timings timings;
    protected final ErrorHandler errorHandler;

    private final TPSTable tpsTable;

    private final SQLFetchOps fetchOps;

    private PluginTask dbCleanTask;

    public SQLDB(
            Supplier<UUID> serverUUIDSupplier,
            Locale locale,
            PlanConfig config,
            NetworkContainer.Factory networkContainerFactory, RunnableFactory runnableFactory,
            PluginLogger logger,
            Timings timings,
            ErrorHandler errorHandler
    ) {
        this.serverUUIDSupplier = serverUUIDSupplier;
        this.locale = locale;
        this.config = config;
        this.networkContainerFactory = networkContainerFactory;
        this.runnableFactory = runnableFactory;
        this.logger = logger;
        this.timings = timings;
        this.errorHandler = errorHandler;

        tpsTable = new TPSTable(this);

        fetchOps = new SQLFetchOps(this);
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
        setOpen(true);
        setupDataSource();
        setupDatabase();
    }

    void setOpen(boolean value) {
        open = value;
    }

    @Override
    public void scheduleClean(long secondsDelay) {
        dbCleanTask = runnableFactory.create("DB Clean Task", new AbsRunnable() {
            @Override
            public void run() {
                try {
                    if (isOpen()) {
                        executeTransaction(new CleanTransaction(serverUUIDSupplier.get(),
                                config.get(TimeSettings.KEEP_INACTIVE_PLAYERS), logger, locale)
                        );
                    }
                } catch (DBOpException e) {
                    errorHandler.log(L.ERROR, this.getClass(), e);
                    cancel();
                }
            }
        }).runTaskTimerAsynchronously(
                TimeAmount.toTicks(secondsDelay, TimeUnit.SECONDS),
                TimeAmount.toTicks(config.get(TimeSettings.CLEAN_DATABASE_PERIOD), TimeUnit.MILLISECONDS)
        );
    }

    Patch[] patches() {
        return new Patch[]{
                new Version10Patch(this),
                new GeoInfoLastUsedPatch(this),
                new SessionAFKTimePatch(this),
                new KillsServerIDPatch(this),
                new WorldTimesSeverIDPatch(this),
                new WorldsServerIDPatch(this),
                new NicknameLastSeenPatch(this),
                new VersionTableRemovalPatch(this),
                new DiskUsagePatch(this),
                new WorldsOptimizationPatch(this),
                new WorldTimesOptimizationPatch(this),
                new KillsOptimizationPatch(this),
                new SessionsOptimizationPatch(this),
                new PingOptimizationPatch(this),
                new NicknamesOptimizationPatch(this),
                new UserInfoOptimizationPatch(this),
                new GeoInfoOptimizationPatch(this),
                new TransferTableRemovalPatch(this),
                new IPHashPatch(this),
                new IPAnonPatch(this),
                new BadAFKThresholdValuePatch(this)
        };
    }

    /**
     * Ensures connection functions correctly and all tables exist.
     * <p>
     * Updates to latest schema.
     *
     * @throws DBInitException if something goes wrong.
     */
    private void setupDatabase() throws DBInitException {
        try {
            executeTransaction(new CreateTablesTransaction());
            registerIndexCreationTask();
            registerPatchTask();
        } catch (DBOpException | IllegalArgumentException e) {
            throw new DBInitException("Failed to set-up Database", e);
        }
    }

    private void registerPatchTask() {
        Patch[] patches = patches();
        try {
            runnableFactory.create("Database Patch", new PatchTask(patches, locale, logger, errorHandler))
                    .runTaskLaterAsynchronously(TimeAmount.toTicks(5L, TimeUnit.SECONDS));
        } catch (Exception ignore) {
            // Task failed to register because plugin is being disabled
        }
    }

    private void registerIndexCreationTask() {
        try {
            runnableFactory.create("Database Index Creation", new AbsRunnable() {
                @Override
                public void run() {
                    executeTransaction(new CreateIndexTransaction(getType()));
                }
            }).runTaskLaterAsynchronously(TimeAmount.toTicks(1, TimeUnit.MINUTES));
        } catch (Exception ignore) {
            // Task failed to register because plugin is being disabled
        }
    }

    public abstract void setupDataSource() throws DBInitException;

    @Override
    public void close() {
        setOpen(false);
        if (dbCleanTask != null) {
            dbCleanTask.cancel();
        }
    }

    public abstract Connection getConnection() throws SQLException;

    @Deprecated
    public abstract void commit(Connection connection);

    public abstract void returnToPool(Connection connection);

    @Deprecated
    public boolean execute(ExecStatement statement) {
        if (!isOpen()) {
            throw new DBOpException("SQL Statement tried to execute while connection closed");
        }

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

    @Deprecated
    public boolean execute(String sql) {
        return execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) {
                // Statement is ready for execution.
            }
        });
    }

    @Deprecated
    public void executeUnsafe(String... statements) {
        Verify.nullCheck(statements);
        for (String statement : statements) {
            try {
                execute(statement);
            } catch (DBOpException e) {
                if (config.isTrue(PluginSettings.DEV_MODE)) {
                    errorHandler.log(L.ERROR, this.getClass(), e);
                }
            }
        }
    }

    @Deprecated
    public void executeBatch(ExecStatement statement) {
        if (!isOpen()) {
            throw new DBOpException("SQL Batch tried to execute while connection closed");
        }

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

    @Deprecated
    public <T> T query(QueryStatement<T> statement) {
        if (!isOpen()) {
            throw new DBOpException("SQL Query tried to execute while connection closed");
        }

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

    @Override
    public <T> T query(Query<T> query) {
        return query.executeQuery(this);
    }

    @Override
    public void executeTransaction(Transaction transaction) {
        transaction.executeTransaction(this);
    }

    @Deprecated
    public TPSTable getTpsTable() {
        return tpsTable;
    }

    @Override
    @Deprecated
    public FetchOperations fetch() {
        return fetchOps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SQLDB sqldb = (SQLDB) o;
        return getType() == sqldb.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType().getName());
    }

    public Supplier<UUID> getServerUUIDSupplier() {
        return serverUUIDSupplier;
    }

    public PlanConfig getConfig() {
        return config;
    }

    public PluginLogger getLogger() {
        return logger;
    }

    public Locale getLocale() {
        return locale;
    }

    public NetworkContainer.Factory getNetworkContainerFactory() {
        return networkContainerFactory;
    }
}
