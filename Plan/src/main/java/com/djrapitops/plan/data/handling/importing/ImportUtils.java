/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling.importing;

import java.util.HashMap;
import java.util.Map;
import static org.bukkit.Bukkit.getPluginManager;

/**
 *
 * @author Risto
 */
public class ImportUtils {
    public static boolean isPluginEnabled(String pluginName) {
        return getPluginManager().isPluginEnabled(pluginName);
    }
    
    public static Map<String, Importer> getImporters() {
        Map<String, Importer> importers = new HashMap<>();
        importers.put("ontime", new OnTimeImporter());
        return importers;
    }
}
