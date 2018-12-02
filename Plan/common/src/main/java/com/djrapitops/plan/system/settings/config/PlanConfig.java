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
package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.data.plugin.PluginsConfigSection;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plugin.config.Config;
import com.djrapitops.plugin.config.ConfigNode;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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

    public int getTimeZoneOffsetHours() {
        if (isTrue(Settings.USE_SERVER_TIME)) {
            int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
            int hourMs = (int) TimeUnit.HOURS.toMillis(1L);
            return -offset / hourMs;
        }
        return 0; // UTC
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