
package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanLiteHook;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.data.UserData;
import java.util.Date;
import java.util.HashMap;
import main.java.com.djrapitops.plan.data.PlanLiteAnalyzedData;
import main.java.com.djrapitops.plan.data.PlanLitePlayerData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import org.bukkit.GameMode;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class PlaceholderUtils {

    /**
     * Gets the HashMap that is used to replace placeholders in Analysis.
     *
     * @param data AnalysisData used to replace the placeholders with
     * @return HashMap that contains string for each placeholder.
     */
    public static HashMap<String, String> getAnalysisReplaceRules(AnalysisData data) {
        HashMap<String, String> replaceMap = new HashMap<>();
        replaceMap.put("%activitypiechart%", data.getActivityChartImgHtml());
        replaceMap.put("%gmpiechart%", data.getGmTimesChartImgHtml());
        replaceMap.put("%gm0%", (int) (data.getGm0Perc() * 100) + "%");
        replaceMap.put("%gm1%", (int) (data.getGm1Perc() * 100) + "%");
        replaceMap.put("%gm2%", (int) (data.getGm2Perc() * 100) + "%");
        replaceMap.put("%gm3%", (int) (data.getGm3Perc() * 100) + "%");
        replaceMap.put("%active%", "" + data.getActive());
        replaceMap.put("%banned%", "" + data.getBanned());
        replaceMap.put("%inactive%", "" + data.getInactive());
        replaceMap.put("%joinleaver%", "" + data.getJoinleaver());
        replaceMap.put("%activitytotal%", "" + data.getTotal());
        replaceMap.put("%playerchartmonth%", data.getPlayersChartImgHtmlMonth());
        replaceMap.put("%playerchartweek%", data.getPlayersChartImgHtmlWeek());
        replaceMap.put("%playerchartday%", data.getPlayersChartImgHtmlDay());
        replaceMap.put("%npday%", data.getNewPlayersDay() + "");
        replaceMap.put("%npweek%", data.getNewPlayersWeek() + "");
        replaceMap.put("%npmonth%", data.getNewPlayersMonth() + "");
        replaceMap.put("%top50commands%", data.getTop50CommandsListHtml());
        replaceMap.put("%avgage%", (data.getAverageAge() != -1) ? "" + data.getAverageAge() : Phrase.DEM_UNKNOWN + "");
        replaceMap.put("%avgplaytime%", FormatUtils.formatTimeAmount("" + data.getAveragePlayTime()));
        replaceMap.put("%totalplaytime%", FormatUtils.formatTimeAmount("" + data.getTotalPlayTime()));
        replaceMap.put("%ops%", "" + data.getOps());
        replaceMap.put("%refresh%", FormatUtils.formatTimeAmountSinceString("" + data.getRefreshDate(), new Date()));
        replaceMap.put("%totallogins%", "" + data.getTotalLoginTimes());
        replaceMap.put("%top20mostactive%", data.getTop20ActivePlayers());
        replaceMap.put("%recentlogins%", data.getRecentPlayers());
        Plan plugin = getPlugin(Plan.class);
        PlanLiteHook hook = plugin.getPlanLiteHook();
        replaceMap.put("%version%", plugin.getDescription().getVersion());
        if (hook != null) {
            replaceMap.put("%planlite%", hook.isEnabled() ? getPlanLiteAnalysisHtml(data.getPlanLiteData()) : "");
        } else {
            replaceMap.put("%planlite%", "");
        }
        return replaceMap;
    }

    public static HashMap<String, String> getPlanLitePlayerReplaceRules(PlanLitePlayerData planLiteData) {
        HashMap<String, String> replaceMap = new HashMap<>();
        PlanLiteHook hook = getPlugin(Plan.class).getPlanLiteHook();
        replaceMap.put("%townylinetown%", hook.hasTowny() ? Html.TOWN.parse(planLiteData.getTown()) : "");
        replaceMap.put("%townylineplotperms%", "");
        replaceMap.put("%townylineplotoptions%", hook.hasTowny() ? Html.PLOT_OPTIONS.parse(planLiteData.getPlotOptions()) : "");
        replaceMap.put("%townylinefriends%", hook.hasTowny() ? Html.FRIENDS.parse(planLiteData.getFriends()) : "");
        replaceMap.put("%factionsline%", hook.hasFactions() ? Html.FACTION.parse(planLiteData.getFaction()) : "");
        replaceMap.put("%totalmoneyline%", hook.hasVault() ? Html.BALANCE.parse(planLiteData.getMoney() + "") : "");
        replaceMap.put("%totalvotesline%", hook.hasSuperbVote() ? Html.VOTES.parse(planLiteData.getVotes() + "") : "");
        return replaceMap;
    }

    /**
     * Gets the HashMap that is used to replace placeholders.
     *
     * @param data UserData used to replace the placeholders with
     * @return HashMap that contains string for each placeholder.
     */
    public static HashMap<String, String> getInspectReplaceRules(UserData data) {
        HashMap<String, String> replaceMap = new HashMap<>();
        replaceMap.put("%uuid%", "" + data.getUuid());
        replaceMap.put("%lastseen%", FormatUtils.formatTimeStamp("" + data.getLastPlayed()));
        replaceMap.put("%logintimes%", "" + data.getLoginTimes());
        replaceMap.put("%bed%", FormatUtils.formatLocation(data.getBedLocation()));
        replaceMap.put("%geoloc%", data.getDemData().getGeoLocation());
        boolean isActive = AnalysisUtils.isActive(data.getLastPlayed(), data.getPlayTime(), data.getLoginTimes());
        replaceMap.put("%active%", isActive ? Html.ACTIVE.parse() : Html.INACTIVE.parse());
        int age = data.getDemData().getAge();
        replaceMap.put("%age%", (age != -1) ? "" + age : Phrase.DEM_UNKNOWN + "");
        replaceMap.put("%gender%", "" + data.getDemData().getGender().name().toLowerCase());
        HashMap<GameMode, Long> gmTimes = data.getGmTimes();
        replaceMap.put("%gmpiechart%", AnalysisUtils.createGMPieChart(gmTimes));
        long gmZero = gmTimes.get(GameMode.SURVIVAL);
        long gmOne = gmTimes.get(GameMode.CREATIVE);
        long gmTwo = gmTimes.get(GameMode.ADVENTURE);
        long gmThree;
        try {
            Long gm3 = gmTimes.get(GameMode.SPECTATOR);
            if (gm3 == null) {
                gm3 = (long) 0;
            }
            gmThree = gm3;
        } catch (NoSuchFieldError e) {
            gmThree = 0;
        }
        long total = gmZero + gmOne + gmTwo + gmThree;
        replaceMap.put("%gm0%", FormatUtils.formatTimeAmount("" + gmZero));
        replaceMap.put("%gm1%", FormatUtils.formatTimeAmount("" + gmOne));
        replaceMap.put("%gm2%", FormatUtils.formatTimeAmount("" + gmTwo));
        replaceMap.put("%gm3%", FormatUtils.formatTimeAmount("" + gmThree));
        replaceMap.put("%gmtotal%", FormatUtils.formatTimeAmount("" + total));
        replaceMap.put("%ips%", data.getIps().toString());
        replaceMap.put("%nicknames%", FormatUtils.swapColorsToSpan(data.getNicknames().toString()));
        replaceMap.put("%name%", data.getName());
        replaceMap.put("%registered%", FormatUtils.formatTimeStamp("" + data.getRegistered()));
        replaceMap.put("%timeskicked%", "" + data.getTimesKicked());
        replaceMap.put("%playtime%", FormatUtils.formatTimeAmount("" + data.getPlayTime()));
        replaceMap.put("%banned%", data.isBanned() ? Html.BANNED.parse() : "");
        replaceMap.put("%op%", data.isOp() ? Html.OPERATOR.parse() : "");
        replaceMap.put("%isonline%", (data.isOnline()) ? Html.ONLINE.parse() : Html.OFFLINE.parse());
        Plan plugin = getPlugin(Plan.class);
        replaceMap.put("%version%", plugin.getDescription().getVersion());
        PlanLiteHook hook = plugin.getPlanLiteHook();
        if (hook != null) {
            replaceMap.put("%planlite%", hook.isEnabled() ? getPlanLitePlayerHtml(data.getPlanLiteData()) : "");
        } else {
            replaceMap.put("%planlite%", "");
        }
        replaceMap.put("%inaccuratedatawarning%", (new Date().getTime() - data.getRegistered() < 180000) ? Html.WARN_INACCURATE.parse() : "");
        return replaceMap;
    }

    public static HashMap<String, String> getPlanLiteAnalysisReplaceRules(PlanLiteAnalyzedData planLiteData) {
        HashMap<String, String> replaceMap = new HashMap<>();
        PlanLiteHook hook = getPlugin(Plan.class).getPlanLiteHook();
        replaceMap.put("%townyheader%", hook.hasTowny() ? Html.TOP_TOWNS.parse() : "");
        replaceMap.put("%townylist%", hook.hasTowny() ? AnalysisUtils.createTableOutOfHashMap(planLiteData.getTownMap(), 20) : "");
        replaceMap.put("%factionheader%", hook.hasFactions() ? Html.TOP_FACTIONS.parse() : "");
        replaceMap.put("%factionlist%", hook.hasFactions() ? AnalysisUtils.createTableOutOfHashMap(planLiteData.getFactionMap(), 20) : "");
        replaceMap.put("%totalmoneyline%", hook.hasVault() ? Html.TOTAL_BALANCE.parse(planLiteData.getTotalMoney() + "") : "");
        replaceMap.put("%totalvotesline%", hook.hasSuperbVote() ? Html.TOTAL_VOTES.parse(planLiteData.getTotalVotes() + "") : "");
        return replaceMap;
    }

    private static String getPlanLitePlayerHtml(PlanLitePlayerData planLiteData) {
        return HtmlUtils.replacePlaceholders(
                HtmlUtils.getHtmlStringFromResource("planliteplayer.html"), 
                PlaceholderUtils.getPlanLitePlayerReplaceRules(planLiteData)
        );
    }

    private static String getPlanLiteAnalysisHtml(PlanLiteAnalyzedData planLiteData) {
        return HtmlUtils.replacePlaceholders(
                HtmlUtils.getHtmlStringFromResource("planlite.html"), 
                PlaceholderUtils.getPlanLiteAnalysisReplaceRules(planLiteData)
        );
    }
}
