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
public class ASkyBlockIslands extends PluginData {

    private final ASkyBlockAPI api;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param aaAPI ASkyBlockAPI
     */
    public ASkyBlockIslands(ASkyBlockAPI aaAPI) {
        super("ASkyBlock", "island_count", AnalysisType.HTML);
        this.api = aaAPI;
        super.setIcon("street-view");
        super.setPrefix("Islands: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        int count = api.getIslandCount();
        if (count > 0) {
            return parseContainer(modifierPrefix, count + "");
        }
        return parseContainer(modifierPrefix, "0");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        //Not used ever
        return -1;
    }

}
