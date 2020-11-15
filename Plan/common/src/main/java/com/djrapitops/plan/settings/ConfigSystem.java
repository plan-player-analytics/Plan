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
package com.djrapitops.plan.settings;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.TimeZoneUtility;
import com.djrapitops.plan.settings.config.paths.FormatSettings;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.debug.*;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;

/**
 * System for Config and other user customizable options.
 *
 * @author Rsl1122
 */
@Singleton
public abstract class ConfigSystem implements SubSystem {

    protected final PlanFiles files;
    protected final PlanConfig config;
    protected final Theme theme;
    protected final PluginLogger logger;
    protected final ErrorLogger errorLogger;

    protected ConfigSystem(
            PlanFiles files,
            PlanConfig config,
            Theme theme,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.files = files;
        this.config = config;
        this.theme = theme;
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    public PlanConfig getConfig() {
        return config;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public void enable() {
        try {
            copyDefaults();
            config.reorder(Arrays.asList(
                    "Server", "Network", "Plugin", "Database", "Webserver",
                    "Data_gathering", "Time", "Display_options", "Formatting", "World_aliases", "Export", "Plugins"
            ));
            config.save();

            if (logger.getDebugLogger() instanceof CombineDebugLogger) {
                setDebugMode();
            }

            checkWrongTimeZone();
        } catch (IOException e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder().whatToDo("Fix write permissions to " + config.getConfigFilePath()).build());
            throw new EnableException("Failed to save default config: " + e.getMessage(), e);
        }
        theme.enable();
    }

    public void checkWrongTimeZone() {
        String timeZone = config.get(FormatSettings.TIMEZONE);
        Optional<TimeZone> foundTZ = TimeZoneUtility.parseTimeZone(timeZone);
        if (!foundTZ.isPresent()) {
            logger.warn("Config: " + FormatSettings.TIMEZONE.getPath() + " has invalid value '" + timeZone + "', using GMT+0");
        }
    }

    private void setDebugMode() {
        CombineDebugLogger debugLogger = (CombineDebugLogger) logger.getDebugLogger();

        String debugMode = config.get(PluginSettings.DEBUG);

        List<DebugLogger> loggers = new ArrayList<>();
        if (Verify.containsOne(debugMode, "true", "both", "all", "console")) {
            loggers.add(new ConsoleDebugLogger(logger));
        }
        if (Verify.containsOne(debugMode, "true", "both", "all", "file")) {
            loggers.add(new FolderTimeStampFileDebugLogger(files.getLogsFolder(), () -> errorLogger));
        }
        if (Verify.containsOne(debugMode, "true", "both", "all", "memory")) {
            loggers.add(debugLogger.getDebugLogger(MemoryDebugLogger.class).orElse(new MemoryDebugLogger()));
        }
        debugLogger.setDebugLoggers(loggers.toArray(new DebugLogger[0]));
    }

    /**
     * Copies default values from file in jar to Config.
     *
     * @throws IOException If file can't be read or written.
     */
    protected abstract void copyDefaults() throws IOException;

    @Override
    public void disable() {
        theme.disable();
    }

    public void reload() {
        try {
            config.read();
        } catch (IOException e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder().whatToDo("Fix read permissions to " + config.getConfigFilePath()).build());
        }
    }
}
