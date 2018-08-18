package com.djrapitops.plan.modules;

import com.djrapitops.plugin.IPlugin;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for defining Abstract Plugin Framework utilities.
 *
 * @author Rsl1122
 */
@Module
public class APFModule {

    @Provides
    DebugLogger provideDebugLogger(IPlugin plugin) {
        return plugin.getDebugLogger();
    }

    @Provides
    PluginLogger providePluginLogger(IPlugin plugin) {
        return plugin.getPluginLogger();
    }

    @Provides
    ErrorHandler provideErrorHandler(IPlugin plugin) {
        return plugin.getErrorHandler();
    }

    @Provides
    Timings provideTimings(IPlugin plugin) {
        return plugin.getTimings();
    }

    @Provides
    RunnableFactory provideRunnableFactory(IPlugin plugin) {
        return plugin.getRunnableFactory();
    }

}