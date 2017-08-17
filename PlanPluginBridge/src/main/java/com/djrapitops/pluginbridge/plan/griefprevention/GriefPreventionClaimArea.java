package com.djrapitops.pluginbridge.plan.griefprevention;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * PluginData class for GriefPrevention-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class GriefPreventionClaimArea extends PluginData {

    private final DataStore dataStore;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param dataStore DataStore of GriefPrevention
     */
    public GriefPreventionClaimArea(DataStore dataStore) {
        super("GriefPrevention", "claim_area", AnalysisType.INT_TOTAL);
        this.dataStore = dataStore;
        super.setAnalysisOnly(false);
        super.setIcon("map-o");
        super.setPrefix("Claimed Area: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        Verify.nullCheck(uuid);
        int area = dataStore.getClaims().stream()
                .filter(Objects::nonNull)
                .filter(claim -> uuid.equals(claim.ownerID))
                .map(Claim::getArea).mapToInt(i -> i).sum();
        return parseContainer(modifierPrefix, Integer.toString(area));
    }

    @Override
    public Serializable getValue(UUID uuid) {
        Verify.nullCheck(uuid);
        return dataStore.getClaims().stream()
                .filter(Objects::nonNull)
                .filter(claim -> uuid.equals(claim.ownerID))
                .map(Claim::getArea).mapToInt(i -> i).sum();
    }
}
