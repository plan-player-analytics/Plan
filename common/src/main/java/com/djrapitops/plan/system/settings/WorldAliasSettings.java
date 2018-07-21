/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings;

import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.config.ConfigNode;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for managing Bukkit side config settings for World Aliases.
 *
 * @author Rsl1122
 */
public class WorldAliasSettings {

    private WorldAliasSettings() {
        /* Hide Constructor */
    }

    /**
     * Used to get all World aliases in the config
     *
     * @return Map: Original name, Alias
     */
    public static Map<String, String> getAliases() {
        ConfigNode aliasSect = getAliasSection();

        Map<String, String> aliasMap = new HashMap<>();
        for (Map.Entry<String, ConfigNode> world : aliasSect.getChildren().entrySet()) {
            aliasMap.put(world.getKey(), world.getValue().getString());
        }
        return aliasMap;
    }

    private static ConfigNode getAliasSection() {
        Config config = ConfigSystem.getConfig();
        return config.getConfigNode(Settings.WORLD_ALIASES.getPath());
    }

    /**
     * Adds a new World to the config section.
     * <p>
     * If exists does not override old value.
     *
     * @param world World name
     */
    public static void addWorld(String world) {
        ConfigNode aliasSect = getAliasSection();

        String previousValue = aliasSect.getConfigNode(world).getValue();
        if (Verify.isEmpty(previousValue)) {
            aliasSect.set(world, world);
            Processing.submitNonCritical(() -> {
                try {
                    aliasSect.save();
                } catch (IOException e) {
                    Log.toLog(WorldAliasSettings.class, e);
                }
            });
        }
    }
}
