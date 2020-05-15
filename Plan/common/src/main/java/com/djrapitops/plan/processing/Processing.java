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
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import dagger.Lazy;
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
        criticalExecutor = createExecutor(2, "Plan Critical-pool-%d");
    }

    protected ExecutorService createExecutor(int i, String s) {
        return Executors.newFixedThreadPool(i,
                new BasicThreadFactory.Builder()
                        .namingPattern(s)
                        .uncaughtExceptionHandler((thread, throwable) ->
                                errorLogger.log(L.WARN, throwable, ErrorContext.builder().build())
                        ).build());
    }

    public void submit(Runnable runnable) {
        if (runnable instanceof CriticalRunnable) {
            submitCritical(runnable);
            return;
        }
        submitNonCritical(runnable);
    }

    public void submitNonCritical(Runnable runnable) {
        if (runnable == null || nonCriticalExecutor.isShutdown()) {
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            runnable.run();
            return true;
        }, nonCriticalExecutor).handle(this::exceptionHandlerNonCritical);
    }

    public void submitCritical(Runnable runnable) {
        if (runnable == null) return;
        CompletableFuture.supplyAsync(() -> {
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
            errorLogger.log(L.WARN, throwable.getCause(), ErrorContext.builder().build());
        }
        return t;
    }

    private <T> T exceptionHandlerCritical(T t, Throwable throwable) {
        if (throwable != null) {
            errorLogger.log(L.ERROR, throwable.getCause(), ErrorContext.builder().build());
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
        if (criticalExecutor.isShutdown()) {
            criticalExecutor = createExecutor(2, "Plan Critical-pool-%d");
        }
    }

    @Override
    public void disable() {
        shutdownNonCriticalExecutor();
        shutdownCriticalExecutor();
        ensureShutdown();
        logger.info(locale.get().getString(PluginLang.DISABLED_PROCESSING_COMPLETE));
    }

    private void shutdownNonCriticalExecutor() {
        nonCriticalExecutor.shutdown();
    }

    private void shutdownCriticalExecutor() {
        List<Runnable> criticalTasks = criticalExecutor.shutdownNow();
        logger.info(locale.get().getString(PluginLang.DISABLED_PROCESSING, criticalTasks.size()));
        for (Runnable runnable : criticalTasks) {
            if (runnable == null) continue;
            try {
                runnable.run();
            } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
                errorLogger.log(L.WARN, e, ErrorContext.builder().build());
            }
        }
    }

    private void ensureShutdown() {
        try {
            if (!nonCriticalExecutor.isTerminated() && !nonCriticalExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                nonCriticalExecutor.shutdownNow();
            }
            if (!criticalExecutor.isTerminated()) {
                criticalExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Processing shutdown thread interrupted: " + e.getMessage());
            nonCriticalExecutor.shutdownNow();
            criticalExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
