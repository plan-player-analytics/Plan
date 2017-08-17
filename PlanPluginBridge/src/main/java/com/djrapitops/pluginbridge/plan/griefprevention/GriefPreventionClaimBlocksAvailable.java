package com.djrapitops.pluginbridge.plan.griefprevention;

import main.java.com.djrapitops.plan.data.additional.PluginData;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.PlayerData;

import java.io.Serializable;
import java.util.UUID;

/**
 * PluginData class for GriefPrevention-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class GriefPreventionClaimBlocksAvailable extends PluginData {

    private final DataStore dataStore;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param dataStore DataStore of GriefPrevention
     */
    public GriefPreventionClaimBlocksAvailable(DataStore dataStore) {
        super("GriefPrevention", "claim_available");
        this.dataStore = dataStore;
        super.setIcon("map-o");
        super.setPrefix("Claim blocks available: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        PlayerData data = dataStore.getPlayerData(uuid);
        int blocks = data.getAccruedClaimBlocks() + data.getBonusClaimBlocks() + dataStore.getGroupBonusBlocks(uuid);
        return parseContainer(modifierPrefix, Integer.toString(blocks));
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }
}
