/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.utilities.Verify;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
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
@Singleton
public class ServerSpecificSettings {

    private final Lazy<PlanPlugin> plugin;
    private final Lazy<PlanConfig> config;

    private final PluginLogger logger;

    @Inject
    public ServerSpecificSettings(
            Lazy<PlanPlugin> plugin,
            Lazy<PlanConfig> config,
            PluginLogger logger
    ) {
        this.plugin = plugin;
        this.config = config;
        this.logger = logger;
    }

    public void updateSettings(Map<String, String> settings) throws IOException {
        logger.debug("Checking new settings..");

        boolean changedSomething = false;
        PlanConfig planConfig = config.get();
        for (Map.Entry<String, String> setting : settings.entrySet()) {
            try {
                String path = setting.getKey();
                if ("sender".equals(path)) {
                    continue;
                }
                String stringValue = setting.getValue();
                Object value = getValue(stringValue);
                String currentValue = planConfig.getString(path);
                if (stringValue.equals(currentValue)) {
                    continue;
                }
                planConfig.set(path, value);
                logger.debug("  " + path + ": " + value);
            } catch (NullPointerException ignored) {
            }
            changedSomething = true;
        }

        if (changedSomething) {
            planConfig.save();
            logger.info("----------------------------------");
            logger.info("The Received Bungee Settings changed the config values, restarting Plan..");
            logger.info("----------------------------------");
            plugin.get().reloadPlugin(true);
        } else {
            logger.debug("Settings up to date");
        }
    }

    private Object getValue(String value) {
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

    public void addOriginalBukkitSettings(UUID serverUUID, Map<String, Object> settings) throws IOException {
        PlanConfig planConfig = config.get();
        if (!Verify.isEmpty(planConfig.getString("Servers." + serverUUID + ".ServerName"))) {
            return;
        }
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            planConfig.set("Servers." + serverUUID + "." + entry.getKey(), entry.getValue());
        }
        planConfig.save();
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
        String path = getPath(serverUUID, setting);
        return config.get().getBoolean(path);
    }

    public String getString(UUID serverUUID, Settings setting) {
        String path = getPath(serverUUID, setting);
        return config.get().getString(path);
    }

    public Integer getInt(UUID serverUUID, Settings setting) {
        String path = getPath(serverUUID, setting);
        return config.get().getInt(path);
    }

    public void set(UUID serverUUID, Settings setting, Object value) throws IOException {
        String path = getPath(serverUUID, setting);
        PlanConfig planConfig = config.get();
        planConfig.set(path, value);
        planConfig.save();
    }
}
