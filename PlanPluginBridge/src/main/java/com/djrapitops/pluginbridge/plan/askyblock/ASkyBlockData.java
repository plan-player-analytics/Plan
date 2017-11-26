/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.askyblock;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import main.java.com.djrapitops.plan.data.additional.AnalysisContainer;
import main.java.com.djrapitops.plan.data.additional.ContainerSize;
import main.java.com.djrapitops.plan.data.additional.InspectContainer;
import main.java.com.djrapitops.plan.data.additional.PluginData;

import java.util.Collection;
import java.util.UUID;

/**
 * PluginData for ASkyBlock plugin.
 *
 * @author Rsl1122
 */
public class ASkyBlockData extends PluginData {

    private final ASkyBlockAPI api;

    public ASkyBlockData(ASkyBlockAPI api) {
        super(ContainerSize.THIRD, "ASkyBlock");
        super.setPluginIcon("street-view");
        super.setIconColor("light-blue");
        this.api = api;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) throws Exception {
        if (api.hasIsland(uuid)) {
            String islandName = api.getIslandName(uuid);
            int level = api.getIslandLevel(uuid);
            int resetsLeft = api.getResetsLeft(uuid);

            inspectContainer.addValue(getWithIcon("Island Name", "street-view", "green"), islandName);
            inspectContainer.addValue(getWithIcon("Island Level", "street-view", "amber"), level);
            inspectContainer.addValue(getWithIcon("Island Resets Left", "refresh", "green"), resetsLeft);
        } else {
            inspectContainer.addValue(getWithIcon("Island Name", "street-view", "green"), "No Island");
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) throws Exception {
        int islandCount = api.getIslandCount();
        String islandWorldName = api.getIslandWorld().getName();

        analysisContainer.addValue(getWithIcon("Island World", "map-o", "green"), islandWorldName);
        analysisContainer.addValue(getWithIcon("Island Count", "street-view", "green"), islandCount);

        return analysisContainer;
    }
}