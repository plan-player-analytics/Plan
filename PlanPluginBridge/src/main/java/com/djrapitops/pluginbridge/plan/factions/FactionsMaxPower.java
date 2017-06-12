package com.djrapitops.pluginbridge.plan.factions;

import com.massivecraft.factions.entity.MPlayer;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;

/**
 * PluginData class for Factions-plugin.
 *
 * Registered to the plugin by FactionsHook
 *
 * Gives Max Power Integer as value.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see FactionsHook
 */
public class FactionsMaxPower extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public FactionsMaxPower() {
        super("Factions", "maxpower");
        super.setPrefix("Max Power: ");
        super.setIcon("bolt");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        MPlayer mPlayer = MPlayer.get(uuid);
        return parseContainer(modifierPrefix, FormatUtils.cutDecimals(mPlayer.getPowerMax()));
    }

    @Override
    public Serializable getValue(UUID uuid) {
        MPlayer mPlayer = MPlayer.get(uuid);
        return mPlayer.getPowerMax();
    }

}
