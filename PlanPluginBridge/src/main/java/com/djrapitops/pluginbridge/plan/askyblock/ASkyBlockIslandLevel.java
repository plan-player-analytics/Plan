package com.djrapitops.pluginbridge.plan.askyblock;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;

/**
 * PluginData class for ASkyBlock-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class ASkyBlockIslandLevel extends PluginData {

    private final ASkyBlockAPI api;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param aaAPI ASkyBlockAPI
     */
    public ASkyBlockIslandLevel(ASkyBlockAPI aaAPI) {
        super("ASkyBlock", "island_level", AnalysisType.INT_AVG);
        this.api = aaAPI;
        super.setAnalysisOnly(false);
        super.setIcon("street-view");
        super.setPrefix("Island Level: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        if (api.hasIsland(uuid)) {
            int level = api.getIslandLevel(uuid);
            return parseContainer(modifierPrefix, level + "");
        }
        return parseContainer(modifierPrefix, "No Island");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        if (api.hasIsland(uuid)) {
            return api.getIslandLevel(uuid);
        }
        return -1;
    }

}
