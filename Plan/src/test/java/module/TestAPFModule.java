package module;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plugin.IPlugin;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.debug.CombineDebugLogger;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.debug.MemoryDebugLogger;
import com.djrapitops.plugin.logging.error.CombineErrorHandler;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.task.thread.ThreadRunnableFactory;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Dagger Test module for defining Abstract Plugin Framework utilities.
 *
 * @author Rsl1122
 */
@Module
public class TestAPFModule {
    @Provides
    @Singleton
    IPlugin provideIPlugin(PlanPlugin plugin) {
        return plugin;
    }

    @Provides
    @Named("currentVersion")
    @Singleton
    String provideCurrentVersion() {
        return "0.0.1-TEST";
    }

    @Provides
    @Singleton
    ColorScheme provideColorScheme() {
        return new ColorScheme("§2", "§7", "§f");
    }

    @Provides
    @Singleton
    DebugLogger provideDebugLogger() {
        return new CombineDebugLogger(new MemoryDebugLogger());
    }

    @Provides
    @Singleton
    PluginLogger providePluginLogger() {
        return new TestPluginLogger();
    }

    @Provides
    @Singleton
    ErrorHandler provideErrorHandler(PluginLogger logger) {
        return new CombineErrorHandler(new ConsoleErrorLogger(logger));
    }

    @Provides
    @Singleton
    Timings provideTimings(DebugLogger debugLogger) {
        return new Timings(debugLogger);
    }

    @Provides
    @Singleton
    RunnableFactory provideRunnableFactory() {
        return new ThreadRunnableFactory();
    }

}