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
import com.djrapitops.plan.system.settings.paths.*;
import com.djrapitops.plan.system.settings.paths.key.Setting;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Class for managing Config setting transfer from Bungee to Bukkit servers.
 *
 * @author Rsl1122
 */
@Singleton
@Deprecated
public class NetworkSettings {

    private static final String SPLIT = ";;SETTING;;";
    private static final String VAL_SPLIT = ";;VALUE;;";

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

        processing.submitNonCritical(this::loadFromDatabase);
    }

    public void placeSettingsToDB() {
        if (!Check.isBungeeAvailable() && !Check.isVelocityAvailable()) {
            return;
        }

        processing.submitNonCritical(this::placeToDatabase);
    }

    void loadFromDatabase() {
        logger.debug("NetworkSettings: Fetch Config settings from database..");
        Optional<String> encodedConfigSettings = dbSystem.get().getDatabase().transfer().getEncodedConfigSettings();

        if (!encodedConfigSettings.isPresent()) {
            logger.debug("NetworkSettings: No Config settings in database.");
            return;
        }

        String configSettings = Base64Util.decode(encodedConfigSettings.get());
        Map<String, String> pathValueMap = getPathsAndValues(configSettings);

        logger.debug("NetworkSettings: Updating Settings");
        try {
            serverSpecificSettings.updateSettings(pathValueMap);
        } catch (IOException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private Map<String, String> getPathsAndValues(String configSettings) {
        Map<String, String> pathValueMap = new HashMap<>();

        logger.debug("NetworkSettings: Reading Config String..");
        String[] settings = configSettings.split(SPLIT);
        UUID thisServerUUID = serverInfo.get().getServerUUID();
        for (String settingAndVal : settings) {
            String[] settingValSplit = settingAndVal.split(VAL_SPLIT);
            String setting = settingValSplit[0];
            String[] pathSplit = setting.split(":");
            String path;
            if (pathSplit.length == 2) {
                UUID serverUUID = UUID.fromString(pathSplit[0]);
                if (!thisServerUUID.equals(serverUUID)) {
                    continue;
                }
                path = pathSplit[1];
            } else {
                path = setting;
            }

            String value = settingValSplit.length == 2 ? settingValSplit[1] : "";
            pathValueMap.put(path, value);
        }
        return pathValueMap;
    }

    void placeToDatabase() {
        Map<String, Object> configValues = getConfigValues();

        logger.debug("NetworkSettings: Building Base64 String..");
        StringBuilder transferBuilder = new StringBuilder();
        int size = configValues.size();
        int i = 0;
        for (Map.Entry<String, Object> entry : configValues.entrySet()) {
            String path = entry.getKey();
            String value = entry.getValue().toString();

            transferBuilder.append(path).append(VAL_SPLIT).append(value);

            if (i < size - 1) {
                transferBuilder.append(SPLIT);
            }
            i++;
        }

        String base64 = Base64Util.encode(transferBuilder.toString());

        logger.debug("NetworkSettings: Saving Config settings to database..");
        dbSystem.get().getDatabase().transfer().storeConfigSettings(base64);
    }

    private Map<String, Object> getConfigValues() {
        logger.debug("NetworkSettings: Loading Config Values..");
        Map<String, Object> configValues = new HashMap<>();
        addConfigValue(configValues, DatabaseSettings.TYPE, "mysql");
        Setting[] sameStrings = new Setting[]{
                DatabaseSettings.MYSQL_HOST,
                DatabaseSettings.MYSQL_USER,
                DatabaseSettings.MYSQL_PASS,
                DatabaseSettings.MYSQL_DATABASE,
                DatabaseSettings.MYSQL_LAUNCH_OPTIONS,
                FormatSettings.DECIMALS,
                FormatSettings.SECONDS,
                FormatSettings.DAY,
                FormatSettings.DAYS,
                FormatSettings.HOURS,
                FormatSettings.MINUTES,
                FormatSettings.MONTHS,
                FormatSettings.MONTH,
                FormatSettings.YEAR,
                FormatSettings.YEARS,
                FormatSettings.ZERO_SECONDS,
                TimeSettings.USE_SERVER_TIME,
                DisplaySettings.REPLACE_SESSION_ACCORDION_WITH_TABLE,
                DisplaySettings.SESSION_MOST_PLAYED_WORLD_IN_TITLE,
                DisplaySettings.ORDER_WORLD_PIE_BY_PERC,
                DisplaySettings.SESSIONS_PER_PAGE,
                DisplaySettings.PLAYERS_PER_SERVER_PAGE,
                DisplaySettings.PLAYERS_PER_PLAYERS_PAGE,
                FormatSettings.DATE_RECENT_DAYS,
                FormatSettings.DATE_RECENT_DAYS_PATTERN,
                FormatSettings.DATE_CLOCK,
                FormatSettings.DATE_NO_SECONDS,
                FormatSettings.DATE_FULL,
                DisplaySettings.PLAYER_IPS,
                TimeSettings.ACTIVE_LOGIN_THRESHOLD,
                TimeSettings.ACTIVE_PLAY_THRESHOLD,
                DisplaySettings.GAPS_IN_GRAPH_DATA,
                TimeSettings.AFK_THRESHOLD,
                DataGatheringSettings.GEOLOCATIONS,
                PluginSettings.KEEP_LOGS_DAYS,
                TimeSettings.KEEP_INACTIVE_PLAYERS,
                TimeSettings.PING_SERVER_ENABLE_DELAY,
                TimeSettings.PING_PLAYER_LOGIN_DELAY
        };
        logger.debug("NetworkSettings: Adding Config Values..");
        PlanConfig planConfig = config.get();
        for (Setting setting : sameStrings) {
            addConfigValue(configValues, setting, planConfig.get(setting));
        }
        addConfigValue(configValues, DatabaseSettings.MYSQL_PORT, planConfig.get(DatabaseSettings.MYSQL_PORT));
        addServerSpecificValues(configValues);
        return configValues;
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
