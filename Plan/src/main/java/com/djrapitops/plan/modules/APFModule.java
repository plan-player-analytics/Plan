package com.djrapitops.plan.modules;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plugin.IPlugin;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Dagger module for defining Abstract Plugin Framework utilities.
 *
 * @author Rsl1122
 */
@Module
public class APFModule {
    @Provides
    @Singleton
    IPlugin provideIPlugin(PlanPlugin plugin) {
        return plugin;
    }

    @Provides
    @Named("currentVersion")
    @Singleton
    String provideCurrentVersion(IPlugin plugin) {
        return plugin.getVersion();
    }

    @Provides
    @Singleton
    ColorScheme provideColorScheme(PlanPlugin plugin) {
        return plugin.getColorScheme();
    }

    @Provides
    @Singleton
    DebugLogger provideDebugLogger(IPlugin plugin) {
        return plugin.getDebugLogger();
    }

    @Provides
    @Singleton
    PluginLogger providePluginLogger(IPlugin plugin) {
        return plugin.getPluginLogger();
    }

    @Provides
    @Singleton
    ErrorHandler provideErrorHandler(IPlugin plugin) {
        return plugin.getErrorHandler();
    }

    @Provides
    @Singleton
    Timings provideTimings(IPlugin plugin) {
        return plugin.getTimings();
    }

    @Provides
    @Singleton
    RunnableFactory provideRunnableFactory(IPlugin plugin) {
        return plugin.getRunnableFactory();
    }

}