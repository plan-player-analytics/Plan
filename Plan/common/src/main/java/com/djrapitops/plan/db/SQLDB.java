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
import com.djrapitops.plan.api.exceptions.database.FatalDBException;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.access.transactions.init.CreateIndexTransaction;
import com.djrapitops.plan.db.access.transactions.init.CreateTablesTransaction;
import com.djrapitops.plan.db.access.transactions.init.OperationCriticalTransaction;
import com.djrapitops.plan.db.patches.*;
import com.djrapitops.plan.system.DebugChannels;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.PluginSettings;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.utilities.java.ThrowableUtils;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.BiFunction;
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
    protected final ErrorHandler errorHandler;

    private Supplier<ExecutorService> transactionExecutorServiceProvider;
    private ExecutorService transactionExecutor;

    private final boolean devMode;

    public SQLDB(
            Supplier<UUID> serverUUIDSupplier,
            Locale locale,
            PlanConfig config,
            NetworkContainer.Factory networkContainerFactory, RunnableFactory runnableFactory,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.serverUUIDSupplier = serverUUIDSupplier;
        this.locale = locale;
        this.config = config;
        this.networkContainerFactory = networkContainerFactory;
        this.runnableFactory = runnableFactory;
        this.logger = logger;
        this.errorHandler = errorHandler;

        devMode = config.get(PluginSettings.DEV_MODE);

        this.transactionExecutorServiceProvider = () -> Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Plan " + getClass().getSimpleName() + "-transaction-thread-%d").build());
    }

    @Override
    public void init() {
        List<Runnable> unfinishedTransactions = closeTransactionExecutor(transactionExecutor);
        this.transactionExecutor = transactionExecutorServiceProvider.get();

        setState(State.PATCHING);

        setupDataSource();
        setupDatabase();

        for (Runnable unfinishedTransaction : unfinishedTransactions) {
            transactionExecutor.submit(unfinishedTransaction);
        }

        // If an OperationCriticalTransaction fails open is set to false.
        // See executeTransaction method below.
        if (getState() == State.CLOSED) {
            throw new DBInitException("Failed to set-up Database");
        }
    }

    private List<Runnable> closeTransactionExecutor(ExecutorService transactionExecutor) {
        if (transactionExecutor == null || transactionExecutor.isShutdown() || transactionExecutor.isTerminated()) {
            return Collections.emptyList();
        }
        transactionExecutor.shutdown();
        try {
            Long waitMs = config.getOrDefault(TimeSettings.DB_TRANSACTION_FINISH_WAIT_DELAY, TimeUnit.SECONDS.toMillis(20L));
            if (waitMs > TimeUnit.MINUTES.toMillis(5L)) {
                logger.warn(TimeSettings.DB_TRANSACTION_FINISH_WAIT_DELAY.getPath() + " was set to over 5 minutes, using 5 min instead.");
                waitMs = TimeUnit.MINUTES.toMillis(5L);
            }
            if (!transactionExecutor.awaitTermination(waitMs, TimeUnit.MILLISECONDS)) {
                List<Runnable> unfinished = transactionExecutor.shutdownNow();
                int unfinishedCount = unfinished.size();
                if (unfinishedCount > 0) {
                    logger.warn(unfinishedCount + " unfinished database transactions were not executed.");
                }
                return unfinished;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Collections.emptyList();
    }

    Patch[] patches() {
        return new Patch[]{
                new Version10Patch(),
                new GeoInfoLastUsedPatch(),
                new SessionAFKTimePatch(),
                new KillsServerIDPatch(),
                new WorldTimesSeverIDPatch(),
                new WorldsServerIDPatch(),
                new NicknameLastSeenPatch(),
                new VersionTableRemovalPatch(),
                new DiskUsagePatch(),
                new WorldsOptimizationPatch(),
                new WorldTimesOptimizationPatch(),
                new KillsOptimizationPatch(),
                new SessionsOptimizationPatch(),
                new PingOptimizationPatch(),
                new NicknamesOptimizationPatch(),
                new UserInfoOptimizationPatch(),
                new GeoInfoOptimizationPatch(),
                new TransferTableRemovalPatch(),
                new IPAnonPatch(),
                new BadAFKThresholdValuePatch(),
                new DeleteIPHashesPatch(),
                new ExtensionShowInPlayersTablePatch()
        };
    }

    /**
     * Ensures connection functions correctly and all tables exist.
     * <p>
     * Updates to latest schema.
     */
    private void setupDatabase() {
        executeTransaction(new CreateTablesTransaction());
        for (Patch patch : patches()) {
            executeTransaction(patch);
        }
        executeTransaction(new OperationCriticalTransaction() {
            @Override
            protected void performOperations() {
                if (getState() == State.PATCHING) setState(State.OPEN);
            }
        });
        registerIndexCreationTask();
    }

    private void registerIndexCreationTask() {
        try {
            runnableFactory.create("Database Index Creation", new AbsRunnable() {
                @Override
                public void run() {
                    executeTransaction(new CreateIndexTransaction());
                }
            }).runTaskLaterAsynchronously(TimeAmount.toTicks(1, TimeUnit.MINUTES));
        } catch (Exception ignore) {
            // Task failed to register because plugin is being disabled
        }
    }

    /**
     * Set up the source for connections.
     *
     * @throws DBInitException If the DataSource fails to be initialized.
     */
    public abstract void setupDataSource();

    @Override
    public void close() {
        setState(State.CLOSED);
        closeTransactionExecutor(transactionExecutor);
    }

    public abstract Connection getConnection() throws SQLException;

    public abstract void returnToPool(Connection connection);

    @Override
    public <T> T query(Query<T> query) {
        accessLock.checkAccess();
        return query.executeQuery(this);
    }

    @Override
    public Future<?> executeTransaction(Transaction transaction) {
        if (getState() == State.CLOSED) {
            throw new DBOpException("Transaction tried to execute although database is closed.");
        }

        Exception origin = new Exception();

        return CompletableFuture.supplyAsync(() -> {
            accessLock.checkAccess(transaction);
            if (devMode) {
                logger.getDebugLogger().logOn(DebugChannels.SQL, "Executing: " + transaction.getClass().getSimpleName());
            }
            transaction.executeTransaction(this);
            return CompletableFuture.completedFuture(null);
        }, getTransactionExecutor()).handle(errorHandler(origin));
    }

    private BiFunction<CompletableFuture<Object>, Throwable, CompletableFuture<Object>> errorHandler(Exception origin) {
        return (obj, throwable) -> {
            if (throwable == null) {
                return CompletableFuture.completedFuture(null);
            }
            if (throwable instanceof FatalDBException) {
                setState(State.CLOSED);
            }
            ThrowableUtils.appendEntryPointToCause(throwable, origin);

            errorHandler.log(L.ERROR, getClass(), throwable);
            return CompletableFuture.completedFuture(null);
        };
    }

    private ExecutorService getTransactionExecutor() {
        if (transactionExecutor == null) {
            transactionExecutor = transactionExecutorServiceProvider.get();
        }
        return transactionExecutor;
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

    public NetworkContainer.Factory getNetworkContainerFactory() {
        return networkContainerFactory;
    }

    public void setTransactionExecutorServiceProvider(Supplier<ExecutorService> transactionExecutorServiceProvider) {
        this.transactionExecutorServiceProvider = transactionExecutorServiceProvider;
    }
}
