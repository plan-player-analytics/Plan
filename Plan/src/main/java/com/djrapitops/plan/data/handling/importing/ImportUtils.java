package main.java.com.djrapitops.plan.data.handling.importing;

import com.djrapitops.pluginbridge.plan.importing.OnTimeImporter;
import java.util.HashMap;
import java.util.Map;
import main.java.com.djrapitops.plan.Log;
import static org.bukkit.Bukkit.getPluginManager;

/**
 * This class is responsible for static utility methods used for importing.
 *
 * @author Rsl1122
 * @since 3.2.0
 */
public class ImportUtils {

    /**
     * Checks if a plugin is enabled.
     *
     * @param pluginName Name of the plugin
     * @return true/false
     */
    public static boolean isPluginEnabled(String pluginName) {
        if ("offline".equals(pluginName)) {
            return true;
        }
        return getPluginManager().isPluginEnabled(pluginName);
    }

    /**
     * Used to get all importers for different plugins.
     *
     * @return Map of importers with pluginname in lowercase as key.
     */
    public static Map<String, Importer> getImporters() {
        Map<String, Importer> importers = new HashMap<>();
        try {
            importers.put("ontime", new OnTimeImporter());
            importers.put("offline", new OfflinePlayerImporter());
        } catch (Throwable e) {
            Log.toLog("ImportUtils.getImporters", e);
            Log.error("Plan Plugin Bridge not included in the plugin jar.");
        }

        return importers;
    }
}
