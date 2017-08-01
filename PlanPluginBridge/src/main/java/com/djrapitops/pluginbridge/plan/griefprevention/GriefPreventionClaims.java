package com.djrapitops.pluginbridge.plan.griefprevention;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PluginData class for GriefPrevention-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class GriefPreventionClaims extends PluginData {

    private final DataStore dataStore;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param dataStore DataStore of GriefPrevention
     */
    public GriefPreventionClaims(DataStore dataStore) {
        super("GriefPrevention", "claim_count", AnalysisType.INT_TOTAL);
        this.dataStore = dataStore;
        super.setAnalysisOnly(false);
        super.setIcon("flag-o");
        super.setPrefix("Claims: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        Verify.nullCheck(uuid);
        List<Claim> claims = dataStore.getClaims().stream()
                .filter(Objects::nonNull)
                .filter(claim -> uuid.equals(claim.ownerID))
                .collect(Collectors.toList());
        return parseContainer(modifierPrefix, claims.size()+"");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        Verify.nullCheck(uuid);
        return dataStore.getClaims().stream()
                .filter(Objects::nonNull)
                .filter(claim -> uuid.equals(claim.ownerID))
                .collect(Collectors.toList()).size();
    }
}
