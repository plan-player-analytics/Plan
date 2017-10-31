/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for managing Bukkit side config settings for World Aliases.
 *
 * @author Rsl1122
 */
public class WorldAliasSettings {

    private final Plan plugin;

    /**
     * Constructor.
     *
     * @param plugin Current instance of Plan.
     */
    public WorldAliasSettings(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     * Used to get all World aliases in the config
     *
     * @return Map: Original name, Alias
     */
    public Map<String, String> getAliases() {
        ConfigurationSection aliasSect = getAliasSection();

        Map<String, String> aliasMap = new HashMap<>();
        for (String world : aliasSect.getKeys(false)) {
            aliasMap.put(world, aliasSect.getString(world));
        }
        return aliasMap;
    }

    private ConfigurationSection getAliasSection() {
        FileConfiguration config = plugin.getConfig();
        return config.getConfigurationSection(Settings.WORLD_ALIASES.getPath());
    }

    /**
     * Adds a new World to the config section.
     * <p>
     * If exists does not override old value.
     *
     * @param world World name
     */
    public void addWorld(String world) {
        ConfigurationSection aliasSect = getAliasSection();

        Object previousValue = aliasSect.get(world);
        if (previousValue == null) {
            aliasSect.set(world, world);
        }
        plugin.saveConfig();
    }

    /**
     * Used to get alias of a single world.
     *
     * @param world World name.
     * @return Alias.
     */
    public String getAlias(String world) {
        return getAliasSection().getString(world);
    }
}