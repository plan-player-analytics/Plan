package com.djrapitops.pluginbridge.plan.askyblock;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.PluginData;

/**
 * PluginData class for ASkyBlock-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class ASkyBlockIslandName extends PluginData {

    private final ASkyBlockAPI api;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param aaAPI ASkyBlockAPI
     */
    public ASkyBlockIslandName(ASkyBlockAPI aaAPI) {
        super("ASkyBlock", "island_name");
        this.api = aaAPI;        
        super.setIcon("street-view");
        super.setPrefix("Island name: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        if (api.hasIsland(uuid)) {
            return parseContainer(modifierPrefix, api.getIslandName(uuid) + "");
        }
        return parseContainer(modifierPrefix, "No Island");
        
    }

    @Override
    public Serializable getValue(UUID uuid) {
        //Not used ever
        return -1;
    }

}
