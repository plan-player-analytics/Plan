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
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.exceptions.database.DBClosedException;
import com.djrapitops.plan.exceptions.database.DBInitException;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.exceptions.database.FatalDBException;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.init.CreateIndexTransaction;
import com.djrapitops.plan.storage.database.transactions.init.CreateTablesTransaction;
import com.djrapitops.plan.storage.database.transactions.init.OperationCriticalTransaction;
import com.djrapitops.plan.storage.database.transactions.init.RemoveIncorrectTebexPackageDataPatch;
import com.djrapitops.plan.storage.database.transactions.patches.*;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.java.ThrowableUtils;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import dev.vankka.dependencydownload.ApplicationDependencyManager;
import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.classloader.IsolatedClassLoader;
import dev.vankka.dependencydownload.repository.MavenRepository;
import dev.vankka.dependencydownload.repository.Repository;
import dev.vankka.dependencydownload.resource.DependencyDownloadResource;
import net.playeranalytics.plugin.scheduling.PluginRunnable;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class containing main logic for different data related save and load functionality.
 *
 * @author AuroraLS3
 */
public abstract class SQLDB extends AbstractDatabase {

    private static boolean downloadDriver = true;

    private static final List<Repository> DRIVER_REPOSITORIES = Arrays.asList(
            new MavenRepository("https://repo.papermc.io/repository/maven-public"),
            new MavenRepository("https://repo1.maven.org/maven2")
    );

    private final Supplier<ServerUUID> serverUUIDSupplier;

    protected final Locale locale;
    protected final PlanConfig config;
    protected final PlanFiles files;
    protected final RunnableFactory runnableFactory;
    protected final PluginLogger logger;
    protected final ErrorLogger errorLogger;
    protected final ApplicationDependencyManager applicationDependencyManager;

    protected ClassLoader driverClassLoader;

    private Supplier<ExecutorService> transactionExecutorServiceProvider;
    private ExecutorService transactionExecutor;
    private static final ThreadLocal<StackTraceElement[]> TRANSACTION_ORIGIN = new ThreadLocal<>();

    private final AtomicInteger transactionQueueSize = new AtomicInteger(0);
    private final AtomicBoolean dropUnimportantTransactions = new AtomicBoolean(false);
    private final AtomicBoolean ranIntoFatalError = new AtomicBoolean(false);

    protected SQLDB(
            Supplier<ServerUUID> serverUUIDSupplier,
            Locale locale,
            PlanConfig config,
            PlanFiles files,
            RunnableFactory runnableFactory,
            PluginLogger logger,
            ErrorLogger errorLogger,
            ApplicationDependencyManager applicationDependencyManager
    ) {
        this.serverUUIDSupplier = serverUUIDSupplier;
        this.locale = locale;
        this.config = config;
        this.files = files;
        this.runnableFactory = runnableFactory;
        this.logger = logger;
        this.errorLogger = errorLogger;
        this.applicationDependencyManager = applicationDependencyManager;

        this.transactionExecutorServiceProvider = () -> {
            String nameFormat = "Plan " + getClass().getSimpleName() + "-transaction-thread-%d";
            return Executors.newSingleThreadExecutor(new BasicThreadFactory.Builder()
                    .namingPattern(nameFormat)
                    .uncaughtExceptionHandler((thread, throwable) -> {
                        if (config.isTrue(PluginSettings.DEV_MODE)) {
                            errorLogger.warn(throwable, ErrorContext.builder()
                                    .whatToDo("THIS ERROR IS ONLY LOGGED IN DEV MODE")
                                    .build());
                        }
                    }).build());
        };
    }

    public static void setDownloadDriver(boolean downloadDriver) {
        SQLDB.downloadDriver = downloadDriver;
    }

    protected abstract List<String> getDependencyResource();

    public void downloadDriver() {
        if (downloadDriver) {
            DependencyManager dependencyManager = new DependencyManager(
                    applicationDependencyManager.getDependencyPathProvider(),
                    applicationDependencyManager.getLogger()
            );
            dependencyManager.loadResource(DependencyDownloadResource.parse(getDependencyResource()));

            try {
                dependencyManager.downloadAll(null, DRIVER_REPOSITORIES).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                logger.error("Failed to download " + getType().getName() + "-driver", e);
            }

            IsolatedClassLoader classLoader = new IsolatedClassLoader();
            dependencyManager.load(null, classLoader);

            // Include this dependency manager in the application dependency manager for library cleaning purposes
            applicationDependencyManager.include(dependencyManager);

            this.driverClassLoader = classLoader;
        } else {
            this.driverClassLoader = getClass().getClassLoader();
        }
    }

    public static ThreadLocal<StackTraceElement[]> getTransactionOrigin() {
        return TRANSACTION_ORIGIN;
    }

    @Override
    public void init() {
        List<Runnable> unfinishedTransactions = forceCloseTransactionExecutor();
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

    protected boolean attemptToCloseTransactionExecutor() {
        if (transactionExecutor == null || transactionExecutor.isShutdown() || transactionExecutor.isTerminated()) {
            return true;
        }
        transactionExecutor.shutdown();
        try {
            logger.info(locale.getString(PluginLang.DISABLED_WAITING_TRANSACTIONS));
            Long waitMs = config.getOrDefault(TimeSettings.DB_TRANSACTION_FINISH_WAIT_DELAY, TimeUnit.SECONDS.toMillis(20L));
            if (waitMs > TimeUnit.MINUTES.toMillis(5L)) {
                logger.warn(TimeSettings.DB_TRANSACTION_FINISH_WAIT_DELAY.getPath() + " was set to over 5 minutes, using 5 min instead.");
                waitMs = TimeUnit.MINUTES.toMillis(5L);
            }
            return transactionExecutor.awaitTermination(waitMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
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
                new KillsOptimizationPatch(),
                new NicknamesOptimizationPatch(),
                new TransferTableRemovalPatch(),
                // new BadAFKThresholdValuePatch(),
                new DeleteIPsPatch(),
                new ExtensionShowInPlayersTablePatch(),
                new ExtensionTableRowValueLengthPatch(),
                new CommandUsageTableRemovalPatch(),
                new BadNukkitRegisterValuePatch(),
                new LinkedToSecurityTablePatch(),
                new LinkUsersToPlayersSecurityTablePatch(),
                new LitebansTableHeaderPatch(),
                new UserInfoHostnamePatch(),
                new ServerIsProxyPatch(),
                new ServerTableRowPatch(),
                new PlayerTableRowPatch(),
                new ExtensionTableProviderValuesForPatch(),
                new RemoveIncorrectTebexPackageDataPatch(),
                new ExtensionTableProviderFormattersPatch(),
                new ServerPlanVersionPatch(),
                new RemoveDanglingUserDataPatch(),
                new RemoveDanglingServerDataPatch(),
                new GeoInfoOptimizationPatch(),
                new PingOptimizationPatch(),
                new UserInfoOptimizationPatch(),
                new WorldTimesOptimizationPatch(),
                new SessionsOptimizationPatch(),
                new UserInfoHostnameAllowNullPatch(),
                new RegisterDateMinimizationPatch(),
                new UsersTableNameLengthPatch(),
                new SessionJoinAddressPatch(),
                new RemoveUsernameFromAccessLogPatch(),
                new ComponentColumnToExtensionDataPatch(),
                new BadJoinAddressDataCorrectionPatch(),
                new AfterBadJoinAddressDataCorrectionPatch(),
                new CorrectWrongCharacterEncodingPatch(logger, config),
                new UpdateWebPermissionsPatch(),
                new WebGroupDefaultGroupsPatch(),
                new WebGroupAddMissingAdminGroupPatch(),
                new LegacyPermissionLevelGroupsPatch(),
                new SecurityTableGroupPatch(),
                new ExtensionStringValueLengthPatch(),
                new CookieTableIpAddressPatch()
        };
    }

    /**
     * Ensures connection functions correctly and all tables exist.
     * <p>
     * Updates to latest schema.
     */
    private void setupDatabase() {
        executeTransaction(new OperationCriticalTransaction() {
            @Override
            protected void performOperations() {
                logger.info(locale.getString(PluginLang.DB_SCHEMA_PATCH));
            }
        });
        executeTransaction(new CreateTablesTransaction());
        for (Patch patch : patches()) {
            executeTransaction(patch);
        }
        executeTransaction(new OperationCriticalTransaction() {
            @Override
            protected void performOperations() {
                logger.info(locale.getString(PluginLang.DB_APPLIED_PATCHES));
                if (getState() == State.PATCHING) setState(State.OPEN);
            }
        });
        registerIndexCreationTask();
    }

    private void registerIndexCreationTask() {
        try {
            runnableFactory.create(new PluginRunnable() {
                @Override
                public void run() {
                    if (getState() == State.CLOSED || getState() == State.CLOSING) {
                        cancel();
                        return;
                    }
                    try {
                        executeTransaction(new CreateIndexTransaction());
                    } catch (DBOpException e) {
                        errorLogger.warn(e);
                    }
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

    protected List<Runnable> forceCloseTransactionExecutor() {
        if (transactionExecutor == null || transactionExecutor.isShutdown() || transactionExecutor.isTerminated()) {
            return Collections.emptyList();
        }
        try {
            List<Runnable> unfinished = transactionExecutor.shutdownNow();
            int unfinishedCount = unfinished.size();
            if (unfinishedCount > 0) {
                logger.warn(unfinishedCount + " unfinished database transactions were not executed.");
            }
            return unfinished;
        } finally {
            logger.info(locale.getString(PluginLang.DISABLED_WAITING_TRANSACTIONS_COMPLETE));
        }
    }

    @Override
    public void close() {
        // SQLiteDB Overrides this, so any additions to this should also be reflected there.
        if (getState() == State.OPEN) setState(State.CLOSING);
        if (attemptToCloseTransactionExecutor()) {
            logger.info(locale.getString(PluginLang.DISABLED_WAITING_TRANSACTIONS_COMPLETE));
        } else {
            forceCloseTransactionExecutor();
        }
        unloadDriverClassloader();
        setState(State.CLOSED);
    }

    public abstract Connection getConnection() throws SQLException;

    public abstract void returnToPool(Connection connection);

    @Override
    public <T> T query(Query<T> query) {
        return accessLock.performDatabaseOperation(() -> query.executeQuery(this));
    }

    public <T> T queryWithinTransaction(Query<T> query, Transaction transaction) {
        return accessLock.performDatabaseOperation(() -> query.executeQuery(this), transaction);
    }

    protected void unloadDriverClassloader() {
        // Unloading class loader using close() causes issues when reloading.
        // It is better to leak this memory than crash the plugin on reload.

        driverClassLoader = null;
    }

    @Override
    public CompletableFuture<?> executeTransaction(Transaction transaction) {
        if (getState() == State.CLOSED) {
            throw new DBClosedException("Transaction tried to execute although database is closed.");
        }

        StackTraceElement[] origin = Thread.currentThread().getStackTrace();

        if (determineIfShouldDropUnimportantTransactions(transactionQueueSize.incrementAndGet())
                && transaction instanceof ThrowawayTransaction) {
            // Drop throwaway transaction immediately.
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                TRANSACTION_ORIGIN.set(origin);
                if (getState() == State.CLOSED) return CompletableFuture.completedFuture(null);

                accessLock.performDatabaseOperation(() -> {
                    if (!ranIntoFatalError.get()) {transaction.executeTransaction(this);}
                }, transaction);
                return CompletableFuture.completedFuture(null);
            } finally {
                transactionQueueSize.decrementAndGet();
                TRANSACTION_ORIGIN.remove();
            }
        }, getTransactionExecutor()).exceptionally(errorHandler(transaction, origin));
    }

    private boolean determineIfShouldDropUnimportantTransactions(int queueSize) {
        if (getState() == State.CLOSING) {
            return true;
        }
        boolean dropTransactions = dropUnimportantTransactions.get();
        if (queueSize >= 500 && !dropTransactions) {
            logger.warn("Database queue size: " + queueSize + ", dropping some unimportant transactions. If this keeps happening disable some extensions or optimize MySQL.");
            dropUnimportantTransactions.set(true);
            return true;
        } else if (queueSize < 50 && dropTransactions) {
            dropUnimportantTransactions.set(false);
            return false;
        }
        return dropTransactions;
    }

    private Function<Throwable, CompletableFuture<Object>> errorHandler(Transaction transaction, StackTraceElement[] origin) {
        return throwable -> {
            if (throwable == null) {
                return CompletableFuture.completedFuture(null);
            }
            if (throwable.getCause() instanceof FatalDBException) {
                ranIntoFatalError.set(true);
                logger.error("Database failed to open, " + transaction.getClass().getName() + " failed to be executed.");
                FatalDBException actual = (FatalDBException) throwable.getCause();
                Optional<String> whatToDo = actual.getContext().flatMap(ErrorContext::getWhatToDo);
                whatToDo.ifPresentOrElse(
                        message -> logger.error("What to do: " + message),
                        () -> logger.error("Error msg: " + actual.getMessage())
                );
                setState(State.CLOSED);
            }
            ThrowableUtils.appendEntryPointToCause(throwable, origin);

            ErrorContext errorContext = ErrorContext.builder()
                    .related("Transaction: " + transaction.getClass())
                    .related("DB State: " + getState() + " - fatal: " + ranIntoFatalError.get())
                    .build();
            if (getState() == State.CLOSED) {
                errorLogger.critical(throwable, errorContext);
            } else {
                errorLogger.error(throwable, errorContext);
            }
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

    public Supplier<ServerUUID> getServerUUIDSupplier() {
        return serverUUIDSupplier;
    }

    public void setTransactionExecutorServiceProvider(Supplier<ExecutorService> transactionExecutorServiceProvider) {
        this.transactionExecutorServiceProvider = transactionExecutorServiceProvider;
    }

    public RunnableFactory getRunnableFactory() {
        return runnableFactory;
    }

    public PluginLogger getLogger() {
        return logger;
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean shouldDropUnimportantTransactions() {
        return dropUnimportantTransactions.get();
    }

    @Override
    public int getTransactionQueueSize() {
        return transactionQueueSize.get();
    }
}
