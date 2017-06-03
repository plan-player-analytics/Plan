package main.java.com.djrapitops.plan.data.additional.advancedachievements;

import com.hm.achievement.api.AdvancedAchievementsAPI;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import static org.bukkit.Bukkit.getOfflinePlayers;
import org.bukkit.OfflinePlayer;

/**
 * PluginData class for AdvancedAchievements-plugin.
 *
 * Registered to the plugin by AdvancedAchievementsHook
 *
 * Gives a table of players and achievements achievements.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see AdvancedAchievementsHook
 */
public class AdvancedAchievementsTable extends PluginData {

    private AdvancedAchievementsAPI aaAPI;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * Uses Html to easily parse Html for the table.
     *
     * @param aaAPI AdvancedAchievementsAPI given by AdvancedAchievementsHook
     * @see Html
     */
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
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuidUnused) {
        StringBuilder html = new StringBuilder();
        Map<UUID, OfflinePlayer> offlinePlayers = Arrays.stream(getOfflinePlayers()).filter(p -> p.hasPlayedBefore()).collect(Collectors.toMap(p -> p.getUniqueId(), Function.identity()));
        if (offlinePlayers.isEmpty()) {
            html.append(Html.TABLELINE_2.parse("No Players.", ""));
        } else if (aaAPI.getAdvancedAchievementsVersionCode() >= 520) {
            Map<UUID, Integer> achievementsMap = aaAPI.getPlayersTotalAchievements();
            for (UUID uuid : achievementsMap.keySet()) {
                OfflinePlayer p = offlinePlayers.get(uuid);
                if (p == null) {
                    continue;
                }
                String inspectUrl = HtmlUtils.getInspectUrl(p.getName());
                int achievements = achievementsMap.get(uuid);
                html.append(Html.TABLELINE_2.parse(Html.LINK.parse(inspectUrl, p.getName()), achievements+""));
            }
        } else {
            for (OfflinePlayer p : offlinePlayers.values()) {
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
