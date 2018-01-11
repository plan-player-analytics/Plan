/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.systems.Systems;
import com.djrapitops.plan.utilities.NullCheck;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.File;
import java.io.IOException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public abstract class ConfigSystem implements SubSystem {

    protected final Config config;
    protected final Locale locale;
    protected final Theme theme;

    public ConfigSystem(File configFile) {
        config = new Config(configFile);
        locale = new Locale();
        theme = new Theme();
    }

    public static ConfigSystem getInstance() {
        ConfigSystem configSystem = Systems.getInstance().getConfigSystem();
        NullCheck.check(configSystem, new IllegalStateException("Config System has not been initialized."));
        return configSystem;
    }

    public Theme getThemeSystem() {
        return theme;
    }

    public Config getConfig() {
        return config;
    }

    @Override
    public void enable() throws EnableException {
        try {
            copyDefaults();
            config.save();
        } catch (IOException e) {
            throw new EnableException("Failed to save default config.", e);
        }
        locale.loadLocale();
        theme.enable();
    }

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
        return locale;
    }
}