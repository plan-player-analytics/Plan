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
package com.djrapitops.plan.processing;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import dagger.Lazy;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.*;

@Singleton
public class Processing implements SubSystem {

    private final Lazy<Locale> locale;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    private ExecutorService nonCriticalExecutor;
    private ExecutorService nonCriticalSingleThreadExecutor;
    private ExecutorService criticalExecutor;

    @Inject
    public Processing(
            Lazy<Locale> locale,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.locale = locale;
        this.logger = logger;
        this.errorLogger = errorLogger;
        nonCriticalExecutor = createExecutor(6, "Plan Non critical-pool-%d");
        nonCriticalSingleThreadExecutor = createExecutor(1, "Plan Non critical-pool-single-threaded-%d");
        criticalExecutor = createExecutor(2, "Plan Critical-pool-%d");
    }

    protected ExecutorService createExecutor(int i, String s) {
        return Executors.newFixedThreadPool(i,
                new BasicThreadFactory.Builder()
                        .namingPattern(s)
                        .uncaughtExceptionHandler((thread, throwable) ->
                                errorLogger.warn(throwable, ErrorContext.builder().build())
                        ).build());
    }

    public void submit(Runnable runnable) {
        if (runnable instanceof CriticalRunnable) {
            submitCritical(runnable);
            return;
        }
        submitNonCritical(runnable);
    }

    public CompletableFuture<Boolean> submitNonCritical(Runnable runnable) {
        return submitNonCritical(runnable, false);
    }

    public CompletableFuture<Boolean> submitNonCritical(Runnable runnable, boolean singleThreaded) {
        ExecutorService executorService = singleThreaded ? nonCriticalSingleThreadExecutor : nonCriticalExecutor;
        if (runnable == null || executorService.isShutdown()) {
            return null;
        }
        return CompletableFuture.supplyAsync(() -> {
            runnable.run();
            return true;
        }, executorService).handle(this::exceptionHandlerNonCritical);
    }

    public CompletableFuture<Boolean> submitCritical(Runnable runnable) {
        if (runnable == null) return null;
        return CompletableFuture.supplyAsync(() -> {
            runnable.run();
            return true;
        }, criticalExecutor).handle(this::exceptionHandlerCritical);
    }

    public <T> Future<T> submit(Callable<T> task) {
        if (task instanceof CriticalCallable) {
            return submitCritical(task);
        }
        return submitNonCritical(task);
    }

    public <T> Future<T> submitNonCritical(Callable<T> task) {
        if (task == null || nonCriticalExecutor.isShutdown()) {
            return null;
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, nonCriticalExecutor).handle(this::exceptionHandlerNonCritical);
    }

    private <T> T exceptionHandlerNonCritical(T t, Throwable throwable) {
        if (throwable != null) {
            errorLogger.warn(throwable.getCause(), ErrorContext.builder().build());
        }
        return t;
    }

    private <T> T exceptionHandlerCritical(T t, Throwable throwable) {
        if (throwable != null) {
            errorLogger.error(throwable.getCause(), ErrorContext.builder().build());
        }
        return t;
    }

    public <T> Future<T> submitCritical(Callable<T> task) {
        if (task == null) return null;
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, criticalExecutor).handle(this::exceptionHandlerCritical);
    }

    @Override
    public void enable() {
        if (nonCriticalExecutor.isShutdown()) {
            nonCriticalExecutor = createExecutor(6, "Plan Non critical-pool-%d");
        }
        if (nonCriticalSingleThreadExecutor.isShutdown()) {
            nonCriticalSingleThreadExecutor = createExecutor(1, "Plan Non critical-pool-single-threaded-%d");
        }
        if (criticalExecutor.isShutdown()) {
            criticalExecutor = createExecutor(2, "Plan Critical-pool-%d");
        }
    }

    @Override
    public void disable() {
        shutdownNonCriticalExecutors();
        shutdownCriticalExecutor();
        ensureShutdown();
        logger.info(locale.get().getString(PluginLang.DISABLED_PROCESSING_COMPLETE));
    }

    private void shutdownNonCriticalExecutors() {
        nonCriticalExecutor.shutdownNow();
        nonCriticalSingleThreadExecutor.shutdownNow();
    }

    private void shutdownCriticalExecutor() {
        criticalExecutor.shutdown();
        try {
            if (!criticalExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                List<Runnable> criticalTasks = criticalExecutor.shutdownNow();
                logger.info(locale.get().getString(PluginLang.DISABLED_PROCESSING, criticalTasks.size()));
                for (Runnable runnable : criticalTasks) {
                    if (runnable == null) continue;
                    tryFinishCriticalTask(runnable);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void tryFinishCriticalTask(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
            errorLogger.warn(e, ErrorContext.builder().build());
        }
    }

    private void ensureShutdown() {
        try {
            if (!nonCriticalExecutor.isTerminated()) {
                nonCriticalExecutor.shutdownNow();
            }
            if (!nonCriticalSingleThreadExecutor.isTerminated()) {
                nonCriticalSingleThreadExecutor.shutdownNow();
            }
            if (!criticalExecutor.isTerminated() && !criticalExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                criticalExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Processing shutdown thread interrupted: " + e.getMessage());
            nonCriticalExecutor.shutdownNow();
            nonCriticalSingleThreadExecutor.shutdownNow();
            criticalExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public Executor getCriticalExecutor() {
        return criticalExecutor;
    }
}
