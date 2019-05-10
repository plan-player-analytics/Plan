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
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.settings.paths.key.Setting;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Objects;
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
    private final WorldAliasSettings worldAliasSettings;
    private final PluginLogger logger;

    @Inject
    public PlanConfig(
            @Named("configFile") File file,
            WorldAliasSettings worldAliasSettings,
            PluginLogger logger
    ) {
        super(file);

        this.worldAliasSettings = worldAliasSettings;
        this.logger = logger;

        pluginsConfigSection = new PluginsConfigSection(this);
    }

    public int getTimeZoneOffsetHours() {
        if (isTrue(TimeSettings.USE_SERVER_TIME)) {
            int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
            int hourMs = (int) TimeUnit.HOURS.toMillis(1L);
            return -offset / hourMs;
        }
        return 0; // UTC
    }

    public <T> T get(Setting<T> setting) {
        T value = setting.getValueFrom(this);
        Verify.isTrue(setting.isValid(value), () -> new IllegalStateException(
                "Config value for " + setting.getPath() + " has a bad value: '" + value + "'"
        ));
        return value;
    }

    public <T> T getOrDefault(Setting<T> setting, T defaultValue) {
        try {
            return get(setting);
        } catch (IllegalStateException e) {
            logger.warn(e.getMessage() + ", using '" + defaultValue + "'");
            return defaultValue;
        }
    }

    public boolean isTrue(Setting<Boolean> setting) {
        return get(setting);
    }

    public boolean isFalse(Setting<Boolean> setting) {
        return !isTrue(setting);
    }

    /**
     * If the settings is a String, this method should be used.
     *
     * @return String value of the config setting.
     */
    public String getString(Setting<String> setting) {
        return get(setting);
    }

    /**
     * If the settings is a number, this method should be used.
     *
     * @return Integer value of the config setting
     */
    public int getNumber(Setting<Integer> setting) {
        return get(setting);
    }

    public List<String> getStringList(Setting<List<String>> setting) {
        return get(setting);
    }

    public ConfigNode getConfigNode(Setting<ConfigNode> setting) {
        return get(setting);
    }

    public <T> void set(Setting<T> setting, T value) {
        set(setting.getPath(), value);
    }

    public PluginsConfigSection getPluginsConfigSection() {
        return pluginsConfigSection;
    }

    public WorldAliasSettings getWorldAliasSettings() {
        return worldAliasSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
}