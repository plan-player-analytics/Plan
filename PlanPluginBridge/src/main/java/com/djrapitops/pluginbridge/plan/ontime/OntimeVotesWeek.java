package com.djrapitops.pluginbridge.plan.ontime;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
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
 * Gives Week's Votes Integer as value.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see OnTimeHook
 */
public class OntimeVotesWeek extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public OntimeVotesWeek() {
        super("OnTime", "votes_7d", AnalysisType.LONG_TOTAL);
        super.setAnalysisOnly(false);
        super.setIcon("check");
        super.setPrefix("Votes Last 7d: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        UserData data = Plan.getPlanAPI().getInspectCachedUserDataMap().get(uuid);
        if (data == null) {
            return parseContainer(modifierPrefix, "No votes.");
        }
        String name = data.getName();
        long votesTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.WEEKVOTE);
        if (votesTotal == -1) {
            return parseContainer(modifierPrefix, "No votes.");
        }
        return parseContainer(modifierPrefix, Long.toString(votesTotal));
    }

    @Override
    public Serializable getValue(UUID uuid) {
        UserData data = Plan.getPlanAPI().getInspectCachedUserDataMap().get(uuid);
        if (data == null) {
            return -1L;
        }
        String name = data.getName();
        long votesTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.WEEKVOTE);
        if (votesTotal == -1) {
            return -1L;
        }
        return votesTotal;
    }

}
