package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.data.plugin.PluginsConfigSection;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plugin.config.Config;
import com.djrapitops.plugin.config.ConfigNode;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;

/**
 * Plan configuration file.
 *
 * @author Rsl1122
 */
@Singleton
public class PlanConfig extends Config {

    private final PluginsConfigSection pluginsConfigSection;
    private final NetworkSettings networkSettings;
    private final WorldAliasSettings worldAliasSettings;

    @Inject
    public PlanConfig(
            @Named("configFile") File file,
            NetworkSettings networkSettings,
            WorldAliasSettings worldAliasSettings
    ) {
        super(file);

        this.networkSettings = networkSettings;
        this.worldAliasSettings = worldAliasSettings;

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

    public ConfigNode getConfigNode(Setting setting) {
        return getConfigNode(setting.getPath());
    }

    public void set(Setting setting, Object value) {
        set(setting.getPath(), value);
    }

    public PluginsConfigSection getPluginsConfigSection() {
        return pluginsConfigSection;
    }

    public NetworkSettings getNetworkSettings() {
        return networkSettings;
    }

    public WorldAliasSettings getWorldAliasSettings() {
        return worldAliasSettings;
    }
}