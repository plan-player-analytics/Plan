/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.common.system.settings.config;

import com.djrapitops.plan.common.api.exceptions.EnableException;
import com.djrapitops.plan.common.system.PlanSystem;
import com.djrapitops.plan.common.system.SubSystem;
import com.djrapitops.plan.common.system.file.FileSystem;
import com.djrapitops.plan.common.system.settings.Settings;
import com.djrapitops.plan.common.system.settings.locale.Locale;
import com.djrapitops.plan.common.system.settings.theme.Theme;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.io.IOException;

/**
 * System for Config and other user customizable options.
 *
 * @author Rsl1122
 */
public abstract class ConfigSystem implements SubSystem {

    protected Config config;
    protected final Locale locale;
    protected final Theme theme;

    public ConfigSystem() {
        locale = new Locale();
        theme = new Theme();
    }

    public static ConfigSystem getInstance() {
        ConfigSystem configSystem = PlanSystem.getInstance().getConfigSystem();
        Verify.nullCheck(configSystem, () -> new IllegalStateException("Config System has not been initialized."));
        return configSystem;
    }

    public static Config getConfig() {
        return getInstance().config;
    }

    public Theme getThemeSystem() {
        return getInstance().theme;
    }

    @Override
    public void enable() throws EnableException {
        config = new Config(FileSystem.getConfigFile());
        try {
            copyDefaults();
            config.save();
            Log.setDebugMode(Settings.DEBUG.toString());
        } catch (IOException e) {
            throw new EnableException("Failed to save default config.", e);
        }
        locale.loadLocale();
        theme.enable();
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
        locale.unload();
    }

    public void reload() {
        try {
            config.read();
        } catch (IOException e) {
            Log.toLog(ConfigSystem.class, e);
        }
    }

    public Locale getLocale() {
        return getInstance().locale;
    }
}
