/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan;

import com.djrapitops.plugin.config.IConfig;
import com.djrapitops.plugin.config.fileconfig.IFileConfig;
import com.djrapitops.plugin.utilities.Verify;
import org.bukkit.configuration.file.FileConfiguration;

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

    public void addOriginalBukkitSettings(PlanBungee plugin, UUID serverUUID, Map<String, Object> settings) {
        try {
            IConfig iConfig = plugin.getIConfig();
            IFileConfig config = iConfig.getConfig();
            if (!Verify.isEmpty(config.getString("Servers." + serverUUID + ".ServerName"))) {
                return;
            }
            for (Map.Entry<String, Object> entry : settings.entrySet()) {
                config.set("Servers." + serverUUID + "." + entry.getKey(), entry.getValue());
            }
            iConfig.save();
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    public static void updateSettings(Plan plugin, Map<String, String> settings) {
        Log.debug("Checking new settings..");
        FileConfiguration config = plugin.getConfig();
        boolean changedSomething = false;
        for (Map.Entry<String, String> setting : settings.entrySet()) {
            String path = setting.getKey();
            if ("sender".equals(path)) {
                continue;
            }
            String stringValue = setting.getValue();
            Object value = getValue(stringValue);
            String currentValue = config.getString(path);
            if (currentValue.equals(stringValue)) {
                continue;
            }
            config.set(path, value);
            Log.debug("  " + path + ": " + value);
            changedSomething = true;
        }
        if (changedSomething) {
            plugin.saveConfig();
            Log.info("----------------------------------");
            Log.info("The Received Bungee Settings changed the config values, restarting Plan..");
            Log.info("----------------------------------");
            plugin.restart();
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
        return value;
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

    public Object get(UUID serverUUID, Settings setting) {
        try {
            IFileConfig config = PlanBungee.getInstance().getIConfig().getConfig();
            String path = getPath(serverUUID, setting);
            String value = config.getString(path);
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e) {
                if (Verify.isEmpty(value)) {
                    return config.getInt(value);
                }
                return value;
            }
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return null;
    }

    public boolean getBoolean(UUID serverUUID, Settings setting) {
        try {
            IFileConfig config = PlanBungee.getInstance().getIConfig().getConfig();
            String path = getPath(serverUUID, setting);
            return config.getBoolean(path);
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return false;
    }

    public String getString(UUID serverUUID, Settings setting) {
        try {
            IFileConfig config = PlanBungee.getInstance().getIConfig().getConfig();
            String path = getPath(serverUUID, setting);
            return config.getString(path);
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return null;
    }

    public Integer getInt(UUID serverUUID, Settings setting) {
        try {
            IFileConfig config = PlanBungee.getInstance().getIConfig().getConfig();
            String path = getPath(serverUUID, setting);
            return config.getInt(path);
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return null;
    }

    public void set(UUID serverUUID, Settings setting, Object value) throws IOException {
        IConfig iConfig = PlanBungee.getInstance().getIConfig();
        IFileConfig config = iConfig.getConfig();
        String path = getPath(serverUUID, setting);
        config.set(path, value);
        iConfig.save();
    }
}