package com.djrapitops.plan.system.processing;

import com.djrapitops.plan.PlanHelper;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.util.List;
import java.util.concurrent.*;

public class Processing implements SubSystem {

    private final ExecutorService nonCriticalExecutor;
    private final ExecutorService criticalExecutor;

    public Processing() {
        nonCriticalExecutor = Executors.newFixedThreadPool(6);
        criticalExecutor = Executors.newFixedThreadPool(2);
        saveInstance(nonCriticalExecutor);
        saveInstance(criticalExecutor);
        saveInstance(this);
    }

    public static void submit(Runnable runnable) {
        if (runnable instanceof CriticalRunnable) {
            submitCritical(runnable);
            return;
        }
        submitNonCritical(runnable);
    }

    public static void saveInstance(Object obj) {
        StaticHolder.saveInstance(obj.getClass(), PlanHelper.getInstance().getClass());
    }

    public static void submitNonCritical(Runnable runnable) {
        saveInstance(runnable);
        ExecutorService executor = getInstance().nonCriticalExecutor;
        if (executor.isShutdown()) {
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            runnable.run();
            return true;
        }, executor).handle(Processing::exceptionHandler);
    }

    public static void submitCritical(Runnable runnable) {
        saveInstance(runnable);
        CompletableFuture.supplyAsync(() -> {
            runnable.run();
            return true;
        }, getInstance().criticalExecutor).handle(Processing::exceptionHandler);
    }

    public static void submitNonCritical(Runnable... runnables) {
        for (Runnable runnable : runnables) {
            submitNonCritical(runnable);
        }
    }

    public static void submitCritical(Runnable... runnables) {
        for (Runnable runnable : runnables) {
            submitCritical(runnable);
        }
    }

    public static <T> Future<T> submit(Callable<T> task) {
        saveInstance(task);
        if (task instanceof CriticalCallable) {
            return submitCritical(task);
        }
        return submitNonCritical(task);
    }

    public static <T> Future<T> submitNonCritical(Callable<T> task) {
        saveInstance(task);
        ExecutorService executor = getInstance().nonCriticalExecutor;
        if (executor.isShutdown()) {
            return null;
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, getInstance().nonCriticalExecutor).handle(Processing::exceptionHandler);
    }

    private static <T> T exceptionHandler(T t, Throwable throwable) {
        if (throwable != null) {
            Log.toLog(Processing.class, throwable.getCause());
        }
        return t;
    }

    public static <T> Future<T> submitCritical(Callable<T> task) {
        saveInstance(task);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, getInstance().criticalExecutor).handle(Processing::exceptionHandler);
    }

    public static Processing getInstance() {
        Processing processing = PlanSystem.getInstance().getProcessing();
        Verify.nullCheck(processing, () -> new IllegalStateException("Processing System has not been initialized."));
        return processing;
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
        Log.info("Processing critical unprocessed tasks. (" + criticalTasks.size() + ")");
        for (Runnable runnable : criticalTasks) {
            try {
                runnable.run();
            } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
                Log.toLog(this.getClass(), e);
            }
        }
        Log.info("Processing complete.");
    }
}
