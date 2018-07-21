/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings;

import com.djrapitops.plan.PlanHelper;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Bungee Config manager for Server Settings such as:
 * - WebServer Port
 * - ServerName
 * - Theme Base
 *
 * @author Rsl1122
 */
public class ServerSpecificSettings {

    public static void updateSettings(Map<String, String> settings) {
        Log.debug("Checking new settings..");
        Config config = ConfigSystem.getConfig();

        boolean changedSomething = false;
        for (Map.Entry<String, String> setting : settings.entrySet()) {
            try {
                String path = setting.getKey();
                if ("sender".equals(path)) {
                    continue;
                }
                String stringValue = setting.getValue();
                Object value = getValue(stringValue);
                String currentValue = config.getString(path);
                if (stringValue.equals(currentValue)) {
                    continue;
                }
                config.set(path, value);
                Log.debug("  " + path + ": " + value);
            } catch (NullPointerException ignored) {
            }
            changedSomething = true;
        }

        if (changedSomething) {
            try {
                config.save();
            } catch (IOException e) {
                Log.toLog(ServerSpecificSettings.class, e);
            }
            Log.info("----------------------------------");
            Log.info("The Received Bungee Settings changed the config values, restarting Plan..");
            Log.info("----------------------------------");
            PlanHelper.getInstance().reloadPlugin(true);
        } else {
            Log.debug("Settings up to date");
        }
    }

    private static Object getValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
        }
        if ("true".equalsIgnoreCase(value)) {
            return true;
        } else if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        // Value is a string
        return value;
    }

    public void addOriginalBukkitSettings(UUID serverUUID, Map<String, Object> settings) {
        try {
            Config config = ConfigSystem.getConfig();
            if (!Verify.isEmpty(config.getString("Servers." + serverUUID + ".ServerName"))) {
                return;
            }
            for (Map.Entry<String, Object> entry : settings.entrySet()) {
                config.set("Servers." + serverUUID + "." + entry.getKey(), entry.getValue());
            }
            config.save();
        } catch (IOException e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private String getPath(UUID serverUUID, Settings setting) {
        String path = "Servers." + serverUUID;
        switch (setting) {
            case WEBSERVER_PORT:
                path += ".WebServerPort";
                break;
            case SERVER_NAME:
                path += ".ServerName";
                break;
            case THEME_BASE:
                path += ".ThemeBase";
                break;
            default:
                break;
        }
        return path;
    }

    public boolean getBoolean(UUID serverUUID, Settings setting) {
        Config config = ConfigSystem.getConfig();
        String path = getPath(serverUUID, setting);
        return config.getBoolean(path);
    }

    public String getString(UUID serverUUID, Settings setting) {
        Config config = ConfigSystem.getConfig();
        String path = getPath(serverUUID, setting);
        return config.getString(path);
    }

    public Integer getInt(UUID serverUUID, Settings setting) {
        Config config = ConfigSystem.getConfig();
        String path = getPath(serverUUID, setting);
        return config.getInt(path);
    }

    public void set(UUID serverUUID, Settings setting, Object value) throws IOException {
        Config config = ConfigSystem.getConfig();
        String path = getPath(serverUUID, setting);
        config.set(path, value);
        config.save();
    }
}
