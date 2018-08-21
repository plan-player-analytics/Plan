/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings.network;

import com.djrapitops.plan.api.exceptions.connection.UnsupportedTransferDatabaseException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.ServerSpecificSettings;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

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
public class NetworkSettings {

    private static final String SPLIT = ";;SETTING;;";
    private static final String VAL_SPLIT = ";;VALUE;;";

    public static void loadSettingsFromDB() {
        if (Check.isBungeeAvailable()) {
            return;
        }

        if (BUNGEE_OVERRIDE_STANDALONE_MODE.isTrue() || BUNGEE_COPY_CONFIG.isFalse()) {
            return;
        }

        Processing.submitNonCritical(() -> {
            try {
                new NetworkSettings().loadFromDatabase();
            } catch (UnsupportedTransferDatabaseException e) {
                Log.toLog(NetworkSettings.class, e);
            }
        });
    }

    public static void placeSettingsToDB() {
        if (!Check.isBungeeAvailable()) {
            return;
        }

        Processing.submitCritical(() -> {
            try {
                new NetworkSettings().placeToDatabase();
            } catch (DBOpException | UnsupportedTransferDatabaseException e) {
                Log.toLog(NetworkSettings.class, e);
            }
        });
    }

    public void loadFromDatabase() throws UnsupportedTransferDatabaseException {
        Log.debug("NetworkSettings: Fetch Config settings from database..");
        Optional<String> encodedConfigSettings = Database.getActive().transfer().getEncodedConfigSettings();

        if (!encodedConfigSettings.isPresent()) {
            Log.debug("NetworkSettings: No Config settings in database.");
            return;
        }

        String configSettings = Base64Util.decode(encodedConfigSettings.get());
        Map<String, String> pathValueMap = getPathsAndValues(configSettings);

        Log.debug("NetworkSettings: Updating Settings");
        ServerSpecificSettings.updateSettings(pathValueMap);
    }

    private Map<String, String> getPathsAndValues(String configSettings) {
        Map<String, String> pathValueMap = new HashMap<>();

        Log.debug("NetworkSettings: Reading Config String..");
        String[] settings = configSettings.split(SPLIT);
        UUID thisServerUUID = ServerInfo.getServerUUID();
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

    public void placeToDatabase() throws UnsupportedTransferDatabaseException {
        Map<String, Object> configValues = getConfigValues();

        Log.debug("NetworkSettings: Building Base64 String..");
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

        Log.debug("NetworkSettings: Saving Config settings to database..");
        Database.getActive().transfer().storeConfigSettings(base64);
    }

    private Map<String, Object> getConfigValues() {
        Log.debug("NetworkSettings: Loading Config Values..");
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
        Log.debug("NetworkSettings: Adding Config Values..");
        for (Settings setting : sameStrings) {
            addConfigValue(configValues, setting, setting.toString());
        }
        addConfigValue(configValues, DB_PORT, DB_PORT.getNumber());
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
        Log.debug("NetworkSettings: Adding Server-specific Config Values..");
        ServerSpecificSettings settings = Settings.serverSpecific();

        for (UUID serverUUID : Database.getActive().fetch().getServerUUIDs()) {
            String theme = settings.getString(serverUUID, THEME_BASE);
            Integer port = settings.getInt(serverUUID, WEBSERVER_PORT);
            String name = settings.getString(serverUUID, SERVER_NAME);

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
}
