package com.djrapitops.pluginbridge.plan.ontime;

import com.djrapitops.pluginbridge.plan.FakeOfflinePlayer;
import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import me.edge209.OnTime.OnTimeAPI;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.OfflinePlayer;

/**
 * PluginData class for Ontime-plugin.
 *
 * Registered to the plugin by OnTimeHook
 *
 * Gives Months Referral Integer as value.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see OnTimeHook
 */
public class OntimeReferMonth extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public OntimeReferMonth() {
        super("OnTime", "refer_30d", AnalysisType.LONG_TOTAL);
        super.setAnalysisOnly(false);
        super.setIcon("commenting-o");
        super.setPrefix("Referrals Last 30d: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        UserData data = Plan.getPlanAPI().getInspectCachedUserDataMap().get(uuid);
        if (data == null) {
            return parseContainer(modifierPrefix, "No Referrals.");
        }
        String name = data.getName();
        long referTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.MONTHREFER);
        if (referTotal == -1) {
            return parseContainer(modifierPrefix, "No Referrals.");
        }
        return parseContainer(modifierPrefix, referTotal + "");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        UserData data = Plan.getPlanAPI().getInspectCachedUserDataMap().get(uuid);
        if (data == null) {
            return -1L;
        }
        String name = data.getName();
        long referTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.MONTHREFER);
        if (referTotal == -1) {
            return -1L;
        }
        return referTotal;
    }

}
