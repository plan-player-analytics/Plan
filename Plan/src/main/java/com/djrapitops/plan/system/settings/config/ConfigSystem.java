/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.debug.*;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    protected final ErrorHandler errorHandler;

    public ConfigSystem(
            PlanFiles files,
            PlanConfig config,
            Theme theme,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.files = files;
        this.config = config;
        this.theme = theme;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    public PlanConfig getConfig() {
        return config;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public void enable() throws EnableException {
        try {
            copyDefaults();
            config.save();

            setDebugMode();
        } catch (IOException e) {
            throw new EnableException("Failed to save default config.", e);
        }
        theme.enable();
    }

    private void setDebugMode() {
        String debugMode = config.getString(Settings.DEBUG);

        List<DebugLogger> loggers = new ArrayList<>();
        if (Verify.equalsOne(debugMode, "true", "both", "console")) {
            loggers.add(new ConsoleDebugLogger(logger));
        }
        if (Verify.equalsOne(debugMode, "true", "both", "file")) {
            loggers.add(new FolderTimeStampFileDebugLogger(files.getLogsFolder(), () -> errorHandler));
        }
        if (logger.getDebugLogger() instanceof CombineDebugLogger) {
            CombineDebugLogger debugLogger = (CombineDebugLogger) logger.getDebugLogger();
            loggers.add(debugLogger.getDebugLogger(MemoryDebugLogger.class).orElse(new MemoryDebugLogger()));
            debugLogger.setDebugLoggers(loggers.toArray(new DebugLogger[0]));
        }
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
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }
}
