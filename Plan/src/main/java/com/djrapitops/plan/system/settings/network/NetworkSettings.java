/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings.network;

import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.ServerSpecificSettings;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
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

import static com.djrapitops.plan.system.settings.Settings.*;

/**
 * Class for managing Config setting transfer from Bungee to Bukkit servers.
 *
 * @author Rsl1122
 */
@Singleton
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
        if (planConfig.isTrue(BUNGEE_OVERRIDE_STANDALONE_MODE) || planConfig.isFalse(BUNGEE_COPY_CONFIG)) {
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
        addConfigValue(configValues, DB_TYPE, "mysql");
        Settings[] sameStrings = new Settings[]{
                DB_HOST,
                DB_USER,
                DB_PASS,
                DB_DATABASE,
                DB_LAUNCH_OPTIONS,
                FORMAT_DECIMALS,
                FORMAT_SECONDS,
                FORMAT_DAY,
                FORMAT_DAYS,
                FORMAT_HOURS,
                FORMAT_MINUTES,
                FORMAT_MONTHS,
                FORMAT_MONTH,
                FORMAT_YEAR,
                FORMAT_YEARS,
                FORMAT_ZERO_SECONDS,
                USE_SERVER_TIME,
                DISPLAY_SESSIONS_AS_TABLE,
                APPEND_WORLD_PERC,
                ORDER_WORLD_PIE_BY_PERC,
                MAX_SESSIONS,
                MAX_PLAYERS,
                MAX_PLAYERS_PLAYERS_PAGE,
                PLAYERTABLE_FOOTER,
                FORMAT_DATE_RECENT_DAYS,
                FORMAT_DATE_RECENT_DAYS_PATTERN,
                FORMAT_DATE_CLOCK,
                FORMAT_DATE_NO_SECONDS,
                FORMAT_DATE_FULL,
                DISPLAY_PLAYER_IPS,
                ACTIVE_LOGIN_THRESHOLD,
                ACTIVE_PLAY_THRESHOLD,
                DISPLAY_GAPS_IN_GRAPH_DATA,
                AFK_THRESHOLD_MINUTES,
                DATA_GEOLOCATIONS,
                KEEP_LOGS_DAYS,
                KEEP_INACTIVE_PLAYERS_DAYS,
                PING_SERVER_ENABLE_DELAY,
                PING_PLAYER_LOGIN_DELAY
        };
        logger.debug("NetworkSettings: Adding Config Values..");
        PlanConfig planConfig = config.get();
        for (Settings setting : sameStrings) {
            addConfigValue(configValues, setting, planConfig.getString(setting));
        }
        addConfigValue(configValues, DB_PORT, planConfig.getNumber(DB_PORT));
        addServerSpecificValues(configValues);
        return configValues;
    }

    private void addConfigValue(Map<String, Object> configValues, Settings setting, Object value) {
        if (value != null) {
            configValues.put(setting.getPath(), value);
        }
    }

    private void addConfigValue(Map<String, Object> configValues, UUID serverUUID, Settings setting, Object value) {
        if (value != null) {
            configValues.put(serverUUID + ":" + setting.getPath(), value);
        }
    }

    private void addServerSpecificValues(Map<String, Object> configValues) {
        logger.debug("NetworkSettings: Adding Server-specific Config Values..");

        for (UUID serverUUID : dbSystem.get().getDatabase().fetch().getServerUUIDs()) {
            String theme = serverSpecificSettings.getString(serverUUID, THEME_BASE);
            Integer port = serverSpecificSettings.getInt(serverUUID, WEBSERVER_PORT);
            String name = serverSpecificSettings.getString(serverUUID, SERVER_NAME);

            if (!Verify.isEmpty(theme)) {
                addConfigValue(configValues, serverUUID, THEME_BASE, theme);
            }
            if (port != null && port != 0) {
                addConfigValue(configValues, serverUUID, WEBSERVER_PORT, port);
            }
            if (!Verify.isEmpty(name)) {
                addConfigValue(configValues, serverUUID, SERVER_NAME, name);
            }
        }
    }

    public ServerSpecificSettings getServerSpecificSettings() {
        return serverSpecificSettings;
    }
}
