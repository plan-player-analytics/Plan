package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.data.plugin.PluginsConfigSection;
import com.djrapitops.plugin.config.Config;

import java.io.File;
import java.util.List;

/**
 * Plan configuration file.
 *
 * @author Rsl1122
 */
public class PlanConfig extends Config {

    private final PluginsConfigSection pluginsConfigSection;

    public PlanConfig(File file) {
        super(file);
        pluginsConfigSection = new PluginsConfigSection(this);
    }

    public boolean isTrue(Setting setting) {
        return getBoolean(setting.getPath());
    }

    public boolean isFalse(Setting setting) {
        return !isTrue(setting);
    }

    /**
     * If the settings is a String, this method should be used.
     *
     * @return String value of the config setting.
     */
    public String getString(Setting setting) {
        return getString(setting.getPath());
    }

    /**
     * If the settings is a number, this method should be used.
     *
     * @return Integer value of the config setting
     */
    public int getNumber(Setting setting) {
        return getInt(setting.getPath());
    }

    public List<String> getStringList(Setting setting) {
        return getStringList(setting.getPath());
    }

    public void set(Setting setting, Object value) {
        set(setting.getPath(), value);
    }

    public PluginsConfigSection getPluginsConfigSection() {
        return pluginsConfigSection;
    }
}