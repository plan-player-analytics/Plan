package com.djrapitops.pluginbridge.plan.factions;

import com.massivecraft.factions.entity.MPlayer;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.PluginData;

/**
 * PluginData class for Factions-plugin.
 *
 * Registered to the plugin by FactionsHook
 *
 * Gives a Faction name String as value.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see FactionsHook
 */
public class FactionsFaction extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public FactionsFaction() {
        super("Factions", "faction");
        super.setIcon("flag");
        super.setPrefix("Faction: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        MPlayer mPlayer = MPlayer.get(uuid);
        String faction = mPlayer.getFactionName();
        if (faction.isEmpty()) {
            return parseContainer("", "No Faction.");
        }
        return parseContainer("", faction);
    }

    @Override
    public Serializable getValue(UUID uuid) {
        MPlayer mPlayer = MPlayer.get(uuid);
        return mPlayer.getFactionName();
    }

}
