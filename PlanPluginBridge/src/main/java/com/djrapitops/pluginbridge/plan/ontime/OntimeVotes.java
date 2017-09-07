package com.djrapitops.pluginbridge.plan.ontime;

import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import me.edge209.OnTime.OnTimeAPI;

import java.io.Serializable;
import java.util.UUID;

/**
 * PluginData class for Ontime-plugin.
 *
 * Registered to the plugin by OnTimeHook
 *
 * Gives Total Votes Integer as value.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see OnTimeHook
 */
public class OntimeVotes extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public OntimeVotes() {
        super("OnTime", "votes", AnalysisType.LONG_TOTAL, AnalysisType.LONG_AVG);
        super.setAnalysisOnly(false);
        super.setIcon("check");
        super.setPrefix("Votes All Time: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        String name = getNameOf(uuid);
        long votesTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.TOTALVOTE);
        if (votesTotal == -1) {
            return parseContainer(modifierPrefix, "No votes.");
        }
        return parseContainer(modifierPrefix, Long.toString(votesTotal));
    }

    @Override
    public Serializable getValue(UUID uuid) {
        String name = getNameOf(uuid);
        long votesTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.TOTALVOTE);
        if (votesTotal == -1) {
            return -1L;
        }
        return votesTotal;
    }

}
