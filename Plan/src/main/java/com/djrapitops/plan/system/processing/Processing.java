/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.processing;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.*;

@Singleton
public class Processing implements SubSystem {

    private final Lazy<Locale> locale;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    private final ExecutorService nonCriticalExecutor;
    private final ExecutorService criticalExecutor;

    @Inject
    public Processing(
            Lazy<Locale> locale,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.locale = locale;
        this.logger = logger;
        this.errorHandler = errorHandler;
        nonCriticalExecutor = Executors.newFixedThreadPool(6, new ThreadFactoryBuilder().setNameFormat("Plan Non critical-pool-%d").build());
        criticalExecutor = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("Plan Critical-pool-%d").build());
    }

    public void submit(Runnable runnable) {
        if (runnable instanceof CriticalRunnable) {
            submitCritical(runnable);
            return;
        }
        submitNonCritical(runnable);
    }

    public void submitNonCritical(Runnable runnable) {
        if (nonCriticalExecutor.isShutdown()) {
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            runnable.run();
            return true;
        }, nonCriticalExecutor).handle(this::exceptionHandlerNonCritical);
    }

    public void submitCritical(Runnable runnable) {
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
        if (nonCriticalExecutor.isShutdown()) {
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
            errorHandler.log(L.WARN, Processing.class, throwable.getCause());
        }
        return t;
    }

    private <T> T exceptionHandlerCritical(T t, Throwable throwable) {
        if (throwable != null) {
            errorHandler.log(L.ERROR, Processing.class, throwable.getCause());
        }
        return t;
    }

    public <T> Future<T> submitCritical(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, criticalExecutor).handle(this::exceptionHandlerCritical);
    }

    @Override
    public void enable() throws EnableException {
        if (nonCriticalExecutor.isShutdown()) {
            throw new EnableException("Non Critical ExecutorService was shut down on enable");
        }
        if (criticalExecutor.isShutdown()) {
            throw new EnableException("Critical ExecutorService was shut down on enable");
        }
    }

    @Override
    public void disable() {
        nonCriticalExecutor.shutdown();
        List<Runnable> criticalTasks = criticalExecutor.shutdownNow();
        logger.info(locale.get().getString(PluginLang.DISABLED_PROCESSING, criticalTasks.size()));
        for (Runnable runnable : criticalTasks) {
            try {
                runnable.run();
            } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
                errorHandler.log(L.WARN, this.getClass(), e);
            }
        }
        if (!nonCriticalExecutor.isTerminated()) {
            try {
                nonCriticalExecutor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                nonCriticalExecutor.shutdownNow();
            }
        }
        if (!criticalExecutor.isTerminated()) {
            criticalExecutor.shutdownNow();
        }
        logger.info(locale.get().getString(PluginLang.DISABLED_PROCESSING_COMPLETE));
    }
}
