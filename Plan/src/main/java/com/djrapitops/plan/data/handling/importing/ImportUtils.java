package main.java.com.djrapitops.plan.data.handling.importing;

import java.util.HashMap;
import java.util.Map;
import static org.bukkit.Bukkit.getPluginManager;

/**
 * This class is responsible for static utility methods used for importing.
 *
 * @author Risto
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
        return getPluginManager().isPluginEnabled(pluginName);
    }

    /**
     * Used to get all importers for different plugins.
     *
     * @return Map of importers with pluginname in lowercase as key.
     */
    public static Map<String, Importer> getImporters() {
        Map<String, Importer> importers = new HashMap<>();
        importers.put("ontime", new OnTimeImporter());
        return importers;
    }
}
