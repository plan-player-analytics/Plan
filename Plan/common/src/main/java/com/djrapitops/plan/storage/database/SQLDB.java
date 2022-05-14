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
import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.classloader.IsolatedClassLoader;
import dev.vankka.dependencydownload.repository.Repository;
import dev.vankka.dependencydownload.repository.StandardRepository;
import net.playeranalytics.plugin.scheduling.PluginRunnable;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
            new StandardRepository("https://papermc.io/repo/repository/maven-public/"),
            new StandardRepository("https://repo1.maven.org/maven2/")
    );

    private final Supplier<ServerUUID> serverUUIDSupplier;

    protected final Locale locale;
    protected final PlanConfig config;
    protected final PlanFiles files;
    protected final RunnableFactory runnableFactory;
    protected final PluginLogger logger;
    protected final ErrorLogger errorLogger;

    protected ClassLoader driverClassLoader;

    private Supplier<ExecutorService> transactionExecutorServiceProvider;
    private ExecutorService transactionExecutor;

    protected SQLDB(
            Supplier<ServerUUID> serverUUIDSupplier,
            Locale locale,
            PlanConfig config,
            PlanFiles files,
            RunnableFactory runnableFactory,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.serverUUIDSupplier = serverUUIDSupplier;
        this.locale = locale;
        this.config = config;
        this.files = files;
        this.runnableFactory = runnableFactory;
        this.logger = logger;
        this.errorLogger = errorLogger;

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
            DependencyManager dependencyManager = new DependencyManager(files.getDataDirectory().resolve("libraries"));
            dependencyManager.loadFromResource(getDependencyResource());
            dependencyManager.download(null, DRIVER_REPOSITORIES);

            IsolatedClassLoader classLoader = new IsolatedClassLoader();
            dependencyManager.load(null, classLoader);
            this.driverClassLoader = classLoader;
        } else {
            this.driverClassLoader = getClass().getClassLoader();
        }
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
            logger.info(locale.getString(PluginLang.DISABLED_WAITING_TRANSACTIONS));
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
        } finally {
            logger.info(locale.getString(PluginLang.DISABLED_WAITING_TRANSACTIONS_COMPLETE));
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
                new KillsOptimizationPatch(),
                new NicknamesOptimizationPatch(),
                new TransferTableRemovalPatch(),
                new BadAFKThresholdValuePatch(),
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
                new UsersTableNameLengthPatch()
        };
    }

    /**
     * Ensures connection functions correctly and all tables exist.
     * <p>
     * Updates to latest schema.
     */
    private void setupDatabase() {
        executeTransaction(new CreateTablesTransaction());
        logger.info(locale.getString(PluginLang.DB_SCHEMA_PATCH));
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

    @Override
    public void close() {
        if (getState() == State.OPEN) setState(State.CLOSING);
        closeTransactionExecutor(transactionExecutor);
        unloadDriverClassloader();
        setState(State.CLOSED);
    }

    private void unloadDriverClassloader() {
        // Unloading class loader causes issues when reloading.
        // It is better to leak this memory than crash the plugin on reload.

//        try {
//            if (driverClassLoader instanceof IsolatedClassLoader) {
//                ((IsolatedClassLoader) driverClassLoader).close();
//            }
            driverClassLoader = null;
//        } catch (IOException e) {
//            errorLogger.error(e, ErrorContext.builder().build());
//        }
    }

    public abstract Connection getConnection() throws SQLException;

    public abstract void returnToPool(Connection connection);

    @Override
    public <T> T query(Query<T> query) {
        accessLock.checkAccess();
        return query.executeQuery(this);
    }

    @Override
    public CompletableFuture<?> executeTransaction(Transaction transaction) {
        if (getState() == State.CLOSED) {
            throw new DBOpException("Transaction tried to execute although database is closed.");
        }

        Exception origin = new Exception();

        return CompletableFuture.supplyAsync(() -> {
            accessLock.checkAccess(transaction);
            transaction.executeTransaction(this);
            return CompletableFuture.completedFuture(null);
        }, getTransactionExecutor()).exceptionally(errorHandler(transaction, origin));
    }

    private Function<Throwable, CompletableFuture<Object>> errorHandler(Transaction transaction, Exception origin) {
        return throwable -> {
            if (throwable == null) {
                return CompletableFuture.completedFuture(null);
            }
            if (throwable.getCause() instanceof FatalDBException) {
                logger.error("Database failed to open, " + transaction.getClass().getName() + " failed to be executed.");
                FatalDBException actual = (FatalDBException) throwable.getCause();
                Optional<String> whatToDo = actual.getContext().flatMap(ErrorContext::getWhatToDo);
                whatToDo.ifPresent(message -> logger.error("What to do: " + message));
                if (!whatToDo.isPresent()) logger.error("Error msg: " + actual.getMessage());
                setState(State.CLOSED);
            }
            ThrowableUtils.appendEntryPointToCause(throwable, origin);

            ErrorContext errorContext = ErrorContext.builder()
                    .related("Transaction: " + transaction.getClass())
                    .related("DB State: " + getState())
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
}
