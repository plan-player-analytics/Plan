package com.djrapitops.pluginbridge.plan.griefprevention;

import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import me.ryanhamshire.GriefPrevention.DataStore;

/**
 * PluginData class for GriefPrevention-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class GriefPreventionSoftMuted extends PluginData {

    private final DataStore dataStore;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param dataStore DataStore of GriefPrevention
     */
    public GriefPreventionSoftMuted(DataStore dataStore) {
        super("GriefPrevention", "softmuted", new AnalysisType[]{AnalysisType.BOOLEAN_TOTAL, AnalysisType.BOOLEAN_PERCENTAGE});
        this.dataStore = dataStore;
        super.setAnalysisOnly(false);
        super.setIcon("bell-slash-o");
        super.setPrefix("SoftMuted: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        return parseContainer(modifierPrefix, dataStore.isSoftMuted(uuid) ? "Yes" : "No");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return dataStore.isSoftMuted(uuid);
    }
}
