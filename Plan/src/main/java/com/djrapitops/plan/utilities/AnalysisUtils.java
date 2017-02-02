package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanLiteHook;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.data.ServerData;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.ui.graphs.GMTimesPieChartCreator;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.PlanLiteAnalyzedData;
import main.java.com.djrapitops.plan.data.PlanLitePlayerData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.ui.graphs.ActivityPieChartCreator;
import main.java.com.djrapitops.plan.ui.graphs.PlayerActivityGraphCreator;
import main.java.com.djrapitops.plan.utilities.comparators.MapComparator;
import org.bukkit.GameMode;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class AnalysisUtils {

    /**
     * Creates a GMTimesPieChart image HTML.
     *
     * @param gmTimes HashMap of gamemodes and time in ms how long has been
     * played in them.
     * @return Html img tag with url.
     */
    public static String createGMPieChart(HashMap<GameMode, Long> gmTimes) {
        String url = GMTimesPieChartCreator.createChart(gmTimes);
        return Html.IMG.parse(url);
    }

    /**
     * Creates a GMTimesPieChart image HTML.
     *
     * @param gmTimes HashMap of gamemodes and time in ms how long has been
     * played in them.
     * @param total Total time played in all gamemodes
     * @return Html img tag with url.
     */
    public static String createGMPieChart(HashMap<GameMode, Long> gmTimes, long total) {
        String url = GMTimesPieChartCreator.createChart(gmTimes, total);
        return Html.IMG.parse(url);
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
        replaceMap.put("%active%", AnalysisUtils.isActive(data.getLastPlayed(), data.getPlayTime(), data.getLoginTimes())
                ? Html.ACTIVE.parse() : Html.INACTIVE.parse());
        int age = data.getDemData().getAge();
        replaceMap.put("%age%", (age != -1) ? "" + age : Phrase.DEM_UNKNOWN + "");
        replaceMap.put("%gender%", "" + data.getDemData().getGender().name().toLowerCase());
        HashMap<GameMode, Long> gmTimes = data.getGmTimes();
        replaceMap.put("%gmpiechart%", createGMPieChart(gmTimes));

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
        replaceMap.put("%inaccuratedatawarning%", (new Date().getTime() - data.getRegistered() < 180000)
                ? Html.WARN_INACCURATE.parse() : "");
        return replaceMap;
    }

    static String createPlayerActivityGraph(HashMap<Long, ServerData> rawServerData, long scale) {
        String url = PlayerActivityGraphCreator.createChart(rawServerData, scale);
        return Html.IMG.parse(url);
    }

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

    static boolean isActive(long lastPlayed, long playTime, int loginTimes) {
        int timeToActive = Settings.ANALYSIS_MINUTES_FOR_ACTIVE.getNumber();
        if (timeToActive < 0) {
            timeToActive = 0;
        }
        long twoWeeks = 1209600000;
        if (new Date().getTime() - lastPlayed < twoWeeks) {
            if (loginTimes > 3) {
                if (playTime > 60 * timeToActive) {
                    return true;
                }
            }
        }
        return false;
    }

    static String createActivityPieChart(int totalBanned, int active, int inactive, int joinleaver) {
        String url = ActivityPieChartCreator.createChart(totalBanned, active, inactive, joinleaver);
        return Html.IMG.parse(url);
    }

    static String createTableOutOfHashMap(HashMap<String, Integer> commandUse) {
        return createTableOutOfHashMap(commandUse, 50);
    }

    static String createTableOutOfHashMapLong(HashMap<String, Long> players) {
        return createActivePlayersTable(players, 20);
    }

    static String createTableOutOfHashMap(HashMap<String, Integer> map, int limit) {
        List<String[]> sorted = MapComparator.sortByValue(map);
        String html = Html.TABLE_START.parse();
        if (sorted.isEmpty()) {
            html = Html.ERROR_TABLE.parse();
            return html;
        }
        Collections.reverse(sorted);
        int i = 1;
        for (String[] values : sorted) {
            if (i >= limit) {
                break;
            }
            html += Html.TABLELINE.parse(values[1], values[0]);
            i++;
        }
        html += Html.TABLE_END.parse();
        return html;
    }

    static String createActivePlayersTable(HashMap<String, Long> map, int limit) {
        List<String[]> sorted = MapComparator.sortByValueLong(map);
        String html = Html.TABLE_START.parse();
        if (sorted.isEmpty()) {
            html = Html.ERROR_TABLE.parse()+Html.TABLE_END.parse();
            return html;
        }
        Collections.reverse(sorted);
        int i = 1;
        for (String[] values : sorted) {
            if (i >= limit) {
                break;
            }
            html += Html.TABLELINE.parse(values[1].replaceAll(Html.BUTTON_CLASS.parse(), Html.LINK_CLASS.parse()),FormatUtils.formatTimeAmount(values[0]));
            i++;
        }
        html += Html.TABLE_END.parse();
        return html;
    }

    static String createListStringOutOfHashMapLong(HashMap<String, Long> map, int limit) {
        List<String[]> sorted = MapComparator.sortByValueLong(map);
        String html = "<p>";
        if (sorted.isEmpty()) {
            html = Html.ERROR_LIST.parse();
            return html;
        }
        Collections.reverse(sorted);
        int i = 1;
        for (String[] values : sorted) {
            if (i >= limit) {
                break;
            }
            html += values[1] + " ";
            i++;
        }
        html += "</p>";
        return html;
    }

    private static String getPlanLiteAnalysisHtml(PlanLiteAnalyzedData planLiteData) {
        Scanner scanner = new Scanner(getPlugin(Plan.class).getResource("planlite.html"));
        String html = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            html += line + "\r\n";
        }

        HashMap<String, String> replaceMap = getPlanLiteAnalysisReplaceRules(planLiteData);
        for (String key : replaceMap.keySet()) {
            html = html.replaceAll(key, replaceMap.get(key));
        }
        return html;
    }

    private static HashMap<String, String> getPlanLiteAnalysisReplaceRules(PlanLiteAnalyzedData planLiteData) {
        HashMap<String, String> replaceMap = new HashMap<>();
        PlanLiteHook hook = getPlugin(Plan.class).getPlanLiteHook();
        replaceMap.put("%townyheader%", hook.hasTowny() ? Html.TOP_TOWNS.parse() : "");
        replaceMap.put("%townylist%", hook.hasTowny() ? createTableOutOfHashMap(planLiteData.getTownMap(), 20) : "");
        replaceMap.put("%factionheader%", hook.hasFactions() ? Html.TOP_FACTIONS.parse() : "");
        replaceMap.put("%factionlist%", hook.hasFactions() ? createTableOutOfHashMap(planLiteData.getFactionMap(), 20) : "");
        replaceMap.put("%totalmoneyline%", hook.hasVault() ? Html.TOTAL_BALANCE.parse(planLiteData.getTotalMoney()+"") : "");
        replaceMap.put("%totalvotesline%", hook.hasSuperbVote() ? Html.TOTAL_VOTES.parse(planLiteData.getTotalVotes()+"") : "");
        return replaceMap;
    }

    private static String getPlanLitePlayerHtml(PlanLitePlayerData planLiteData) {
        Scanner scanner = new Scanner(getPlugin(Plan.class).getResource("planliteplayer.html"));
        String html = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            html += line + "\r\n";
        }

        HashMap<String, String> replaceMap = getPlanLitePlayerReplaceRules(planLiteData);
        for (String key : replaceMap.keySet()) {
            html = html.replaceAll(key, replaceMap.get(key));
        }
        return html;
    }

    private static HashMap<String, String> getPlanLitePlayerReplaceRules(PlanLitePlayerData planLiteData) {
        HashMap<String, String> replaceMap = new HashMap<>();
        PlanLiteHook hook = getPlugin(Plan.class).getPlanLiteHook();
        replaceMap.put("%townylinetown%", hook.hasTowny() ? Html.TOWN.parse(planLiteData.getTown()) : "");
        replaceMap.put("%townylineplotperms%", "");
        replaceMap.put("%townylineplotoptions%", hook.hasTowny() ? Html.PLOT_OPTIONS.parse(planLiteData.getPlotOptions()) : "");
        replaceMap.put("%townylinefriends%", hook.hasTowny() ? Html.FRIENDS.parse(planLiteData.getFriends()) : "");
        replaceMap.put("%factionsline%", hook.hasFactions() ? Html.FACTION.parse(planLiteData.getFaction()) : "");
        replaceMap.put("%totalmoneyline%", hook.hasVault() ? Html.BALANCE.parse(planLiteData.getMoney()+"") : "");
        replaceMap.put("%totalvotesline%", hook.hasSuperbVote() ? Html.VOTES.parse(planLiteData.getVotes()+"") : "");
        return replaceMap;
    }
}
