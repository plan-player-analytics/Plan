package com.djrapitops.pluginbridge.plan.advancedachievements;

import com.hm.achievement.api.AdvancedAchievementsAPI;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * PluginData class for AdvancedAchievements-plugin.
 * <p>
 * Registered to the plugin by AdvancedAchievementsHook
 * <p>
 * Gives a table of players and achievements achievements.
 *
 * @author Rsl1122
 * @see AdvancedAchievementsHook
 * @since 3.1.0
 */
public class AdvancedAchievementsTable extends PluginData {

    private final AdvancedAchievementsAPI aaAPI;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     * <p>
     * Uses Html to easily parse Html for the table.
     *
     * @param aaAPI AdvancedAchievementsAPI given by AdvancedAchievementsHook
     * @see Html
     */
    public AdvancedAchievementsTable(AdvancedAchievementsAPI aaAPI) {
        super("AdvancedAchievements", "achievements_table", AnalysisType.HTML);
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
            appendTableLinesForV520Plus(cachedUserData, html);
        } else {
            appendTableLinesForLessThanV520(cachedUserData, html);
        }
        return parseContainer("", html.toString());
    }

    private void appendTableLinesForLessThanV520(Map<UUID, UserData> cachedUserData, StringBuilder html) {
        cachedUserData.values().forEach(uData -> {
            String inspectUrl = HtmlUtils.getRelativeInspectUrl(uData.getName());
            int achievements = aaAPI.getPlayerTotalAchievements(uData.getUuid());
            html.append(Html.TABLELINE_2.parse(Html.LINK.parse(inspectUrl, uData.getName()), achievements));
        });
    }

    private void appendTableLinesForV520Plus(Map<UUID, UserData> cachedUserData, StringBuilder html) {
        Map<UUID, Integer> achievementsMap = aaAPI.getPlayersTotalAchievements();
        for (Map.Entry<UUID, Integer> entry : achievementsMap.entrySet()) {
            UUID uuid = entry.getKey();
            int achievements = entry.getValue();

            UserData uData = cachedUserData.get(uuid);
            if (uData == null) {
                continue;
            }

            String inspectUrl = HtmlUtils.getInspectUrl(uData.getName());
            html.append(Html.TABLELINE_2.parse(Html.LINK.parse(inspectUrl, uData.getName()), achievements));
        }
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }
}
