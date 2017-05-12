package main.java.com.djrapitops.plan.data.additional.ontime;

import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import me.edge209.OnTime.OnTimeAPI;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Rsl1122
 */
public class OntimeVotes extends PluginData {

    public OntimeVotes() {
        super("OnTime", "votes", AnalysisType.INT_TOTAL, AnalysisType.INT_AVG);
        super.setAnalysisOnly(false);
        super.setIcon("check");
        super.setPrefix("Votes All Time: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        if (!offlinePlayer.hasPlayedBefore()) {
            return "";
        }
        String name = offlinePlayer.getName();
        long votesTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.TOTALVOTE);
        if (votesTotal == -1) {
            return parseContainer(modifierPrefix, "No votes.");
        }
        return parseContainer(modifierPrefix, votesTotal + "");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        if (!offlinePlayer.hasPlayedBefore()) {
            return 0;
        }
        String name = offlinePlayer.getName();
        long votesTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.TOTALVOTE);
        if (votesTotal == -1) {
            return 0;
        }
        return votesTotal;
    }

}
