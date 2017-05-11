package main.java.com.djrapitops.plan.data.additional.advancedachievements;

import com.hm.achievement.api.AdvancedAchievementsAPI;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import static org.bukkit.Bukkit.getOfflinePlayers;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Rsl1122
 */
public class AdvancedAchievementsTable extends PluginData {

    private AdvancedAchievementsAPI aaAPI;

    public AdvancedAchievementsTable(AdvancedAchievementsAPI aaAPI) {
        super("AdvancedAchievements", "achievementstable", AnalysisType.HTML);
        this.aaAPI = aaAPI;
        String player = Html.FONT_AWESOME_ICON.parse("user") + " Player";
        String achievements = Html.FONT_AWESOME_ICON.parse("check-circle-o") + " Achievements";
        // analysisOnly true by default.
        super.setPrefix(Html.TABLE_START_2.parse(player, achievements));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        StringBuilder html = new StringBuilder();
        List<OfflinePlayer> offlinePlayers = Arrays.stream(getOfflinePlayers()).filter(p -> p.hasPlayedBefore()).collect(Collectors.toList());
        if (offlinePlayers.isEmpty()) {
            html.append(Html.TABLELINE_2.parse("No Players.",""));
        } else {
            for (OfflinePlayer p : offlinePlayers) {
                String inspectUrl = HtmlUtils.getInspectUrl(p.getName());
                String achievements = aaAPI.getPlayerTotalAchievements(p.getUniqueId()) + "";
                html.append(Html.TABLELINE_2.parse(Html.LINK.parse(inspectUrl, p.getName()), achievements));
            }
        }
        return parseContainer("", html.toString());
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return "";
    }
}
