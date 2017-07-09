package com.djrapitops.pluginbridge.plan.griefprevention;

import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import me.ryanhamshire.GriefPrevention.DataStore;

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
        super("GriefPrevention", "claim_area", new AnalysisType[]{AnalysisType.INT_TOTAL});
        this.dataStore = dataStore;
        super.setAnalysisOnly(false);
        super.setIcon("map-o");
        super.setPrefix("Claimed Area: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        int area = MathUtils.sumInt(dataStore.getClaims().stream().filter(claim -> claim.ownerID.equals(uuid)).map(c -> c.getArea()));
        return parseContainer(modifierPrefix, area + "");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return MathUtils.sumInt(dataStore.getClaims().stream().filter(claim -> claim.ownerID.equals(uuid)).map(c -> c.getArea()));
    }
}
