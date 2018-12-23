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
package com.djrapitops.plan.system.settings.network;

import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.system.settings.paths.PluginSettings;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import com.djrapitops.plan.system.settings.paths.key.Setting;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;

/**
 * Class for managing Config setting transfer from Bungee to Bukkit servers.
 *
 * @author Rsl1122
 */
@Singleton
@Deprecated
public class NetworkSettings {

    private final Lazy<PlanConfig> config;
    private final ServerSpecificSettings serverSpecificSettings;
    private final Processing processing;
    private final Lazy<DBSystem> dbSystem;
    private final Lazy<ServerInfo> serverInfo;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    @Inject
    public NetworkSettings(
            Lazy<PlanConfig> config,
            ServerSpecificSettings serverSpecificSettings,
            Processing processing,
            Lazy<DBSystem> dbSystem,
            Lazy<ServerInfo> serverInfo,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.serverSpecificSettings = serverSpecificSettings;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    public void loadSettingsFromDB() {
        if (Check.isBungeeAvailable() || Check.isVelocityAvailable()) {
            return;
        }

        PlanConfig planConfig = config.get();
        if (planConfig.isFalse(PluginSettings.BUNGEE_COPY_CONFIG)) {
            // Don't load settings if they are overridden.
            return;
        }
    }

    public void placeSettingsToDB() {
        if (!Check.isBungeeAvailable() && !Check.isVelocityAvailable()) {
            return;
        }
    }

    private void addConfigValue(Map<String, Object> configValues, Setting setting, Object value) {
        if (value != null) {
            configValues.put(setting.getPath(), value);
        }
    }

    private void addConfigValue(Map<String, Object> configValues, UUID serverUUID, Setting setting, Object value) {
        if (value != null) {
            configValues.put(serverUUID + ":" + setting.getPath(), value);
        }
    }

    private void addServerSpecificValues(Map<String, Object> configValues) {
        logger.debug("NetworkSettings: Adding Server-specific Config Values..");

        for (UUID serverUUID : dbSystem.get().getDatabase().fetch().getServerUUIDs()) {
            String theme = serverSpecificSettings.getString(serverUUID, DisplaySettings.THEME);
            Integer port = serverSpecificSettings.getInt(serverUUID, WebserverSettings.PORT);
            String name = serverSpecificSettings.getString(serverUUID, PluginSettings.SERVER_NAME);

            if (!Verify.isEmpty(theme)) {
                addConfigValue(configValues, serverUUID, DisplaySettings.THEME, theme);
            }
            if (port != null && port != 0) {
                addConfigValue(configValues, serverUUID, WebserverSettings.PORT, port);
            }
            if (!Verify.isEmpty(name)) {
                addConfigValue(configValues, serverUUID, PluginSettings.SERVER_NAME, name);
            }
        }
    }

    public ServerSpecificSettings getServerSpecificSettings() {
        return serverSpecificSettings;
    }
}
