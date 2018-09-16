/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings;

import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.config.ConfigNode;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class responsible for managing config settings for World Aliases.
 *
 * @author Rsl1122
 */
@Singleton
public class WorldAliasSettings {

    private final Lazy<PlanConfig> config;
    private final Processing processing;
    private final ErrorHandler errorHandler;

    @Inject
    public WorldAliasSettings(
            Lazy<PlanConfig> config,
            Processing processing,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.processing = processing;
        this.errorHandler = errorHandler;
    }

    @Deprecated
    public static Map<String, String> getAliases_Old() {
        return new HashMap<>();
    }

    @Deprecated
    public static void addWorld_Old(String world) {

    }

    private ConfigNode getAliasSection() {
        return config.get().getConfigNode(Settings.WORLD_ALIASES);
    }

    /**
     * Used to get all World aliases in the config
     *
     * @return Map: Original name, Alias
     */
    public Map<String, String> getAliases() {
        ConfigNode aliasSect = getAliasSection();

        Map<String, String> aliasMap = new HashMap<>();
        for (Map.Entry<String, ConfigNode> world : aliasSect.getChildren().entrySet()) {
            aliasMap.put(world.getKey(), world.getValue().getString());
        }
        return aliasMap;
    }

    /**
     * Adds a new World to the config section.
     * <p>
     * If exists does not override old value.
     *
     * @param world World name
     */
    public void addWorld(String world) {
        ConfigNode aliasSect = getAliasSection();

        String previousValue = aliasSect.getConfigNode(world).getValue();
        if (Verify.isEmpty(previousValue)) {
            aliasSect.set(world, world);
            processing.submitNonCritical(() -> {
                try {
                    aliasSect.save();
                } catch (IOException e) {
                    errorHandler.log(L.WARN, WorldAliasSettings.class, e);
                }
            });
        }
    }

    public Map<String, Long> getPlaytimePerAlias(WorldTimes worldTimes) {
        Map<String, Long> playtimePerWorld = worldTimes.getWorldTimes() // WorldTimes Map<String, GMTimes>
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getTotal() // GMTimes.getTotal
                ));

        Map<String, String> aliases = getAliases();

        Map<String, Long> playtimePerAlias = new HashMap<>();
        for (Map.Entry<String, Long> entry : playtimePerWorld.entrySet()) {
            String worldName = entry.getKey();
            long playtime = entry.getValue();

            if (!aliases.containsKey(worldName)) {
                aliases.put(worldName, worldName);
                addWorld(worldName);
            }

            String alias = aliases.get(worldName);

            playtimePerAlias.put(alias, playtimePerAlias.getOrDefault(alias, 0L) + playtime);
        }
        return playtimePerAlias;
    }

    public Map<String, GMTimes> getGMTimesPerAlias(WorldTimes worldTimes) {
        Map<String, String> aliases = getAliases();

        Map<String, GMTimes> gmTimesPerAlias = new HashMap<>();

        String[] gms = GMTimes.getGMKeyArray();

        for (Map.Entry<String, GMTimes> entry : worldTimes.getWorldTimes().entrySet()) {
            String worldName = entry.getKey();
            GMTimes gmTimes = entry.getValue();

            if (!aliases.containsKey(worldName)) {
                aliases.put(worldName, worldName);
                addWorld(worldName);
            }

            String alias = aliases.get(worldName);

            GMTimes aliasGMTimes = gmTimesPerAlias.getOrDefault(alias, new GMTimes());
            for (String gm : gms) {
                aliasGMTimes.addTime(gm, gmTimes.getTime(gm));
            }
            gmTimesPerAlias.put(alias, aliasGMTimes);
        }
        return gmTimesPerAlias;
    }
}
