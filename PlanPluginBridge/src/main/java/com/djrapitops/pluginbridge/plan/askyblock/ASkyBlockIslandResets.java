package com.djrapitops.pluginbridge.plan.askyblock;

import com.djrapitops.pluginbridge.plan.advancedachievements.*;
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
public class ASkyBlockIslandResets extends PluginData {

    private final ASkyBlockAPI api;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param aaAPI AdvancedAchievementsAPI given by AdvancedAchievementsHook
     */
    public ASkyBlockIslandResets(ASkyBlockAPI aaAPI) {
        super("ASkyBlock", "islandresetsleft");
        this.api = aaAPI;        
        super.setIcon("refresh");
        super.setPrefix("Island Resets Left: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        return parseContainer(modifierPrefix, api.getResetsLeft(uuid)+"");
        
    }

    @Override
    public Serializable getValue(UUID uuid) {
        //Not used ever
        return -1;
    }

}
