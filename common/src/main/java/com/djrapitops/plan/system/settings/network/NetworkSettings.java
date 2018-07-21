/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings.network;

import com.djrapitops.plan.api.exceptions.connection.UnsupportedTransferDatabaseException;
import com.djrapitops.plan.api.exceptions.database.DBException;
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

        if (Settings.BUNGEE_OVERRIDE_STANDALONE_MODE.isTrue() || Settings.BUNGEE_COPY_CONFIG.isFalse()) {
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
            } catch (DBException | UnsupportedTransferDatabaseException e) {
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

    public void placeToDatabase() throws DBException, UnsupportedTransferDatabaseException {
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
        addConfigValue(configValues, Settings.DB_TYPE, "mysql");
        Settings[] sameStrings = new Settings[]{
                Settings.DB_HOST,
                Settings.DB_USER,
                Settings.DB_PASS,
                Settings.DB_DATABASE,
                Settings.DB_LAUNCH_OPTIONS,
                Settings.FORMAT_DECIMALS,
                Settings.FORMAT_SECONDS,
                Settings.FORMAT_DAY,
                Settings.FORMAT_DAYS,
                Settings.FORMAT_HOURS,
                Settings.FORMAT_MINUTES,
                Settings.FORMAT_MONTHS,
                Settings.FORMAT_MONTH,
                Settings.FORMAT_YEAR,
                Settings.FORMAT_YEARS,
                Settings.FORMAT_ZERO_SECONDS,
                Settings.USE_SERVER_TIME,
                Settings.DISPLAY_SESSIONS_AS_TABLE,
                Settings.APPEND_WORLD_PERC,
                Settings.ORDER_WORLD_PIE_BY_PERC,
                Settings.MAX_SESSIONS,
                Settings.MAX_PLAYERS,
                Settings.MAX_PLAYERS_PLAYERS_PAGE,
                Settings.PLAYERTABLE_FOOTER,
                Settings.FORMAT_DATE_RECENT_DAYS,
                Settings.FORMAT_DATE_RECENT_DAYS_PATTERN,
                Settings.FORMAT_DATE_CLOCK,
                Settings.FORMAT_DATE_NO_SECONDS,
                Settings.FORMAT_DATE_FULL,
                Settings.DISPLAY_PLAYER_IPS,
                Settings.ACTIVE_LOGIN_THRESHOLD,
                Settings.ACTIVE_PLAY_THRESHOLD,
                Settings.DISPLAY_GAPS_IN_GRAPH_DATA,
                Settings.AFK_THRESHOLD_MINUTES,
                Settings.DATA_GEOLOCATIONS,
                Settings.KEEP_LOGS_DAYS,
                Settings.KEEP_INACTIVE_PLAYERS_DAYS
        };
        Log.debug("NetworkSettings: Adding Config Values..");
        for (Settings setting : sameStrings) {
            addConfigValue(configValues, setting, setting.toString());
        }
        addConfigValue(configValues, Settings.DB_PORT, Settings.DB_PORT.getNumber());
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
            String theme = settings.getString(serverUUID, Settings.THEME_BASE);
            Integer port = settings.getInt(serverUUID, Settings.WEBSERVER_PORT);
            String name = settings.getString(serverUUID, Settings.SERVER_NAME);

            if (!Verify.isEmpty(theme)) {
                addConfigValue(configValues, serverUUID, Settings.THEME_BASE, theme);
            }
            if (port != null && port != 0) {
                addConfigValue(configValues, serverUUID, Settings.WEBSERVER_PORT, port);
            }
            if (!Verify.isEmpty(name)) {
                addConfigValue(configValues, serverUUID, Settings.SERVER_NAME, name);
            }
        }
    }
}
