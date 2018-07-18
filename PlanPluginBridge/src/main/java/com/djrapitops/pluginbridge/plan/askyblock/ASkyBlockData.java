/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.askyblock;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.wasteofplastic.askyblock.ASkyBlockAPI;

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
        setPluginIcon(Icon.called("street-view").of(Color.LIGHT_BLUE).build());
        this.api = api;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        if (api.hasIsland(uuid)) {
            String islandName = api.getIslandName(uuid);
            int level = api.getIslandLevel(uuid);
            int resetsLeft = api.getResetsLeft(uuid);

            inspectContainer.addValue(getWithIcon("Island Name", Icon.called("street-view").of(Color.GREEN)), islandName);
            inspectContainer.addValue(getWithIcon("Island Level", Icon.called("street-view").of(Color.AMBER)), level);
            inspectContainer.addValue(getWithIcon("Island Resets Left", Icon.called("refresh").of(Color.GREEN)), resetsLeft);
        } else {
            inspectContainer.addValue(getWithIcon("Island Name", Icon.called("street-view").of(Color.GREEN)), "No Island");
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        int islandCount = api.getIslandCount();
        String islandWorldName = api.getIslandWorld().getName();

        analysisContainer.addValue(getWithIcon("Island World", Icon.called("map").of(Family.REGULAR).of(Color.GREEN)), islandWorldName);
        analysisContainer.addValue(getWithIcon("Island Count", Icon.called("street-view").of(Color.GREEN)), islandCount);

        return analysisContainer;
    }
}