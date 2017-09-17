/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit;

import com.djrapitops.plugin.config.fileconfig.IFileConfig;
import com.djrapitops.plugin.utilities.Compatibility;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.ServerSpecificSettings;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Fuzzlemann
 */
public class ConfigurationWebAPI extends WebAPI {

    private Map<String, Object> configValues = new HashMap<>();

    @Override
    public Response onRequest(IPlan plugin, Map<String, String> variables) {
        if (Settings.BUNGEE_COPY_CONFIG.isFalse() || Settings.BUNGEE_OVERRIDE_STANDALONE_MODE.isTrue()) {
            return success();
        }
        String key = variables.get("configKey");

        if (key == null) {
            return badRequest("Config Key null");
        }

        String value = variables.get("configValue");

        if (value == null) {
            return badRequest("Config Value null");
        }

        if (value.equals("null")) {
            value = null;
        }

        try {
            IFileConfig config = plugin.getIConfig().getConfig();
            config.set(key, value);
            plugin.getIConfig().save();
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }

        return success();
    }

    public void addConfigValue(Settings setting, Object value) {
        configValues.put(setting.getPath(), value);
    }

    public void setConfigValues(UUID serverUUID, int port) throws WebAPIException {
        if (!Compatibility.isBungeeAvailable()) {
            throw new WebAPIException("Attempted to send config values from Bukkit to Bungee.");
        }
        addConfigValue(Settings.DB_TYPE, "mysql");
        Settings[] sameStrings = new Settings[]{
                Settings.DB_HOST, Settings.DB_USER, Settings.DB_PASS,
                Settings.DB_DATABASE, Settings.FORMAT_DECIMALS, Settings.FORMAT_SECONDS,
                Settings.FORMAT_DAY, Settings.FORMAT_DAYS, Settings.FORMAT_HOURS,
                Settings.FORMAT_MINUTES, Settings.FORMAT_MONTHS, Settings.FORMAT_MONTH,
                Settings.FORMAT_YEAR, Settings.FORMAT_YEARS,
        };
        for (Settings setting : sameStrings) {
            addConfigValue(setting, setting.toString());
        }
        addConfigValue(Settings.DB_PORT, Settings.DB_PORT.getNumber());
        addConfigValue(Settings.WEBSERVER_PORT, port);
        addServerSpecificValues(serverUUID);
    }

    private void addServerSpecificValues(UUID serverUUID) {
        ServerSpecificSettings settings = Settings.serverSpecific();
        addConfigValue(Settings.THEME_BASE, settings.get(serverUUID, Settings.THEME_BASE));
        addConfigValue(Settings.WEBSERVER_PORT, settings.get(serverUUID, Settings.WEBSERVER_PORT));
        addConfigValue(Settings.SERVER_NAME, settings.get(serverUUID, Settings.SERVER_NAME));
    }
}
