package com.djrapitops.pluginbridge.plan.advancedachievements;

import com.hm.achievement.api.AdvancedAchievementsAPI;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

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
        Map<UUID, UserData> cachedUserData = Plan.getPlanAPI().getInspectCachedUserDataMap();
        if (cachedUserData.isEmpty()) {
            html.append(Html.TABLELINE_2.parse("No Players.", ""));
        } else if (aaAPI.getAdvancedAchievementsVersionCode() >= 520) {
            Map<UUID, Integer> achievementsMap = aaAPI.getPlayersTotalAchievements();
            for (UUID uuid : achievementsMap.keySet()) {
                UserData uData = cachedUserData.get(uuid);
                if (uData == null) {
                    continue;
                }
                String inspectUrl = HtmlUtils.getInspectUrl(uData.getName());
                int achievements = achievementsMap.get(uuid);
                html.append(Html.TABLELINE_2.parse(Html.LINK.parse(inspectUrl, uData.getName()), achievements+""));
            }
        } else {
            cachedUserData.values().stream().forEach((uData) -> {
                String inspectUrl = HtmlUtils.getInspectUrl(uData.getName());
                String achievements = aaAPI.getPlayerTotalAchievements(uData.getUuid()) + "";
                html.append(Html.TABLELINE_2.parse(Html.LINK.parse(inspectUrl, uData.getName()), achievements));
            });
        }
        return parseContainer("", html.toString());
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return "";
    }
}
