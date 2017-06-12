package com.djrapitops.pluginbridge.plan.essentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Warps;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;

/**
 * PluginData class for Essentials-plugin.
 *
 * Registered to the plugin by EssentialsHook
 *
 * Gives a list of warps as a String value.
 * 
 * @author Rsl1122
 * @since 3.1.0
 * @see EssentialsHook
 */
public class EssentialsWarps extends PluginData {

    private Essentials essentials;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param essentials Instance of Essentials plugin.
     */
    public EssentialsWarps(Essentials essentials) {
        super("Essentials", "warps", AnalysisType.HTML);
        this.essentials = essentials;
        super.setIcon("map-marker");
        super.setPrefix("Warps: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifier, UUID uuid) {
        Warps warps = essentials.getWarps();
        if (!warps.isEmpty()) {
            return parseContainer("", warps.getList().toString());
        }
        return parseContainer("", "No Warps.");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        Warps warps = essentials.getWarps();
        if (!warps.isEmpty()) {
            return warps.getList().toString();
        }
        return "No Warps.";
    }

}
