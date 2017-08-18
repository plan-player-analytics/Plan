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
 * <p>
 * Registered to the plugin by OnTimeHook
 * <p>
 * Gives Total Referral Integer as value.
 *
 * @author Rsl1122
 * @see OnTimeHook
 * @since 3.1.0
 */
public class OntimeRefer extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public OntimeRefer() {
        super("OnTime", "refer", AnalysisType.LONG_TOTAL, AnalysisType.LONG_AVG);
        super.setAnalysisOnly(false);
        super.setIcon("commenting-o");
        super.setPrefix("Referrals All Time: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        UserData data = Plan.getPlanAPI().getInspectCachedUserDataMap().get(uuid);
        if (data == null) {
            return parseContainer(modifierPrefix, "No Referrals.");
        }
        String name = data.getName();
        long referTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.TOTALREFER);
        if (referTotal == -1) {
            return parseContainer(modifierPrefix, "No Referrals.");
        }
        return parseContainer(modifierPrefix, Long.toString(referTotal));
    }

    @Override
    public Serializable getValue(UUID uuid) {
        UserData data = Plan.getPlanAPI().getInspectCachedUserDataMap().get(uuid);
        if (data == null) {
            return -1L;
        }
        String name = data.getName();
        long referTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.TOTALREFER);
        if (referTotal == -1) {
            return -1L;
        }
        return referTotal;
    }

}
