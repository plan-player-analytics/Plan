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
public class OntimeRefer extends PluginData {

    public OntimeRefer() {
        super("OnTime", "refer", AnalysisType.INT_TOTAL, AnalysisType.INT_AVG);
        super.setAnalysisOnly(false);
        super.setIcon("commenting-o");
        super.setPrefix("Referrals All Time: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        if (!offlinePlayer.hasPlayedBefore()) {
            return "";
        }
        String name = offlinePlayer.getName();
        long referTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.TOTALREFER);
        if (referTotal == -1) {
            return parseContainer(modifierPrefix, "No Referrals.");
        }
        return parseContainer(modifierPrefix, referTotal + "");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        if (!offlinePlayer.hasPlayedBefore()) {
            return 0;
        }
        String name = offlinePlayer.getName();
        long referTotal = OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.TOTALREFER);
        if (referTotal == -1) {
            return 0;
        }
        return referTotal;
    }

}
