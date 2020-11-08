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
package utilities.dagger;

import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.debug.ConsoleDebugLogger;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Module;
import dagger.Provides;
import utilities.mocks.objects.TestRunnableFactory;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class TestAPFModule {

    @Provides
    @Named("currentVersion")
    @Singleton
    String provideCurrentVersion() {
        return "0.0.1";
    }

    @Provides
    @Singleton
    ColorScheme provideColorScheme() {
        return new ColorScheme(
                "", "", ""
        );
    }

    @Provides
    @Singleton
    DebugLogger provideDebugLogger(PluginLogger logger) {
        return new ConsoleDebugLogger(logger);
    }

    @Provides
    @Singleton
    PluginLogger providePluginLogger() {
        return new TestPluginLogger();
    }

    @Provides
    @Singleton
    ErrorHandler provideErrorHandler(ErrorLogger errorLogger) {
        return errorLogger;
    }

    @Provides
    @Singleton
    Timings provideTimings(DebugLogger debugLogger) {
        return new Timings(debugLogger);
    }

    @Provides
    @Singleton
    RunnableFactory provideRunnableFactory() {
        return new TestRunnableFactory();
    }

}
