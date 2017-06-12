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
 * Gives Month's Votes Integer as value.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see OnTimeHook
 */
public class OntimeVotesMonth extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public OntimeVotesMonth() {
        super("OnTime", "votes_30d", AnalysisType.LONG_TOTAL);
        super.setAnalysisOnly(false);
        super.setIcon("check");
        super.setPrefix("Votes Last 30d: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        if (!offlinePlayer.hasPlayedBefore()) {
            return "";
        }
        String name = offlinePlayer.getName();
        long votesTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.MONTHVOTE);
        if (votesTotal == -1) {
            return parseContainer(modifierPrefix, "No votes.");
        }
        return parseContainer(modifierPrefix, votesTotal + "");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        if (!offlinePlayer.hasPlayedBefore()) {
            return -1L;
        }
        String name = offlinePlayer.getName();
        long votesTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.MONTHVOTE);
        if (votesTotal == -1) {
            return -1L;
        }
        return votesTotal;
    }

}
