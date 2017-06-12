package com.djrapitops.pluginbridge.plan.ontime;

import java.io.Serializable;
import java.util.UUID;
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
 * Gives Week's Referral Integer as value.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see OnTimeHook
 */
public class OntimeReferWeek extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public OntimeReferWeek() {
        super("OnTime", "refer_7d", AnalysisType.LONG_TOTAL);
        super.setAnalysisOnly(false);
        super.setIcon("commenting-o");
        super.setPrefix("Referrals Last 7d: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        if (!offlinePlayer.hasPlayedBefore()) {
            return "";
        }
        String name = offlinePlayer.getName();
        long referTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.WEEKREFER);
        if (referTotal == -1) {
            return parseContainer(modifierPrefix, "No Referrals.");
        }
        return parseContainer(modifierPrefix, referTotal + "");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        if (!offlinePlayer.hasPlayedBefore()) {
            return -1L;
        }
        String name = offlinePlayer.getName();
        long referTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.WEEKREFER);
        if (referTotal == -1) {
            return -1L;
        }
        return referTotal;
    }

}
