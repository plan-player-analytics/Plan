/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan;

import com.djrapitops.plugin.config.fileconfig.IFileConfig;
import main.java.com.djrapitops.plan.api.IPlan;

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

    public void updateSettings(IPlan plugin, Map<String, String> settings) {
        try {
            IFileConfig config = plugin.getIConfig().getConfig();
            boolean changedSomething = false;
            for (Map.Entry<String, String> setting : settings.entrySet()) {
                String path = setting.getKey();
                String value = setting.getValue();
                String currentValue = config.getString(path);
                if (currentValue.equals(value)) {
                    continue;
                }
                config.set(path, value);
                changedSomething = true;
            }
            if (changedSomething) {
                plugin.getIConfig().save();
                Log.info("----------------------------------");
                Log.info("The Received Bungee Settings changed the config values, restarting Plan..");
                Log.info("----------------------------------");
                plugin.onDisable();
                plugin.onEnable();
            }
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    public Object get(UUID serverUUID, Settings setting) {
        return null;
    }
}