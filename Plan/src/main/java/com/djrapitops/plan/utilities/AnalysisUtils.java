package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.data.ServerData;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.ui.graphs.GMTimesPieChartCreator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import main.java.com.djrapitops.plan.ui.graphs.ActivityPieChartCreator;
import main.java.com.djrapitops.plan.utilities.comparators.MapComparator;
import org.bukkit.GameMode;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class AnalysisUtils {

    public static String createGMPieChart(HashMap<GameMode, Long> gmTimes) {
        String url = GMTimesPieChartCreator.createChart(gmTimes);
        return "<img src=\"" + url + "\">";
    }
    public static String createGMPieChart(HashMap<GameMode, Long> gmTimes, long total) {
        String url = GMTimesPieChartCreator.createChart(gmTimes, total);
        return "<img src=\"" + url + "\">";
    }

    public static HashMap<String, String> getInspectReplaceRules(UserData data) {
        HashMap<String, String> replaceMap = new HashMap<>();
        replaceMap.put("%uuid%", "" + data.getUuid());
        replaceMap.put("%logintimes%", "" + data.getLoginTimes());
        replaceMap.put("%bed%", FormatUtils.formatLocation(data.getBedLocation()));
        replaceMap.put("%geoloc%", data.getDemData().getGeoLocation());
        int age = data.getDemData().getAge();
        replaceMap.put("%age%", (age != -1) ? "" + age : "Not known");
        replaceMap.put("%gender%", "" + data.getDemData().getGender().name().toLowerCase());
        HashMap<GameMode, Long> gmTimes = data.getGmTimes();
        replaceMap.put("%gmpiechart%", createGMPieChart(gmTimes));
        long gmZero = gmTimes.get(GameMode.SURVIVAL);
        long gmOne = gmTimes.get(GameMode.CREATIVE);
        long gmTwo = gmTimes.get(GameMode.ADVENTURE);
        long gmThree = gmTimes.get(GameMode.SPECTATOR);
        long total = gmZero + gmOne + gmTwo + gmThree;
        replaceMap.put("%gm0%", FormatUtils.formatTimeAmount("" + gmZero));
        replaceMap.put("%gm1%", FormatUtils.formatTimeAmount("" + gmOne));
        replaceMap.put("%gm2%", FormatUtils.formatTimeAmount("" + gmTwo));
        replaceMap.put("%gm3%", FormatUtils.formatTimeAmount("" + gmThree));
        replaceMap.put("%gmtotal%", FormatUtils.formatTimeAmount("" + total));
        replaceMap.put("%ips%", data.getIps().toString());
        replaceMap.put("%nicknames%", data.getNicknames().toString());
        replaceMap.put("%name%", data.getName());
        replaceMap.put("%registered%", FormatUtils.formatTimeStamp("" + data.getRegistered()));
        replaceMap.put("%timeskicked%", "" + data.getTimesKicked());
        replaceMap.put("%playtime%", FormatUtils.formatTimeAmount("" + data.getPlayTime()));
        replaceMap.put("%banned%", data.isBanned() ? "Banned" : "Not Banned");
        replaceMap.put("%op%", data.isOp() ? ", Operator (Op)" : "");
        return replaceMap;
    }

    static String createPlayerActivityGraph(HashMap<Long, ServerData> rawServerData, long time) {
        return "<img src=\"" + "\">";
    }

    public static HashMap<String, String> getAnalysisReplaceRules(AnalysisData data) {
        HashMap<String, String> replaceMap = new HashMap<>();
        replaceMap.put("%activitypiechart%", data.getActivityChartImgHtml());
        replaceMap.put("%gmpiechart%", data.getGmTimesChartImgHtml());
        replaceMap.put("%gm0%", (int) data.getGm0Perc()*100+"%");
        replaceMap.put("%gm1%", (int) data.getGm1Perc()*100+"%");
        replaceMap.put("%gm2%", (int) data.getGm2Perc()*100+"%");
        replaceMap.put("%gm3%", (int) data.getGm3Perc()*100+"%");
        replaceMap.put("%active%", "" + data.getActive());
        replaceMap.put("%banned%", "" + data.getBanned());
        replaceMap.put("%inactive%", "" + data.getInactive());
        replaceMap.put("%activitytotal%", "" + data.getTotal());
        replaceMap.put("%playerchart%", data.getPlayersChartImgHtml());
        replaceMap.put("%top50commands%", data.getTop50CommandsListHtml());
        replaceMap.put("%avgage%", ""+data.getAverageAge());
        replaceMap.put("%avgplaytime%", FormatUtils.formatTimeAmount(""+data.getAveragePlayTime()));
        replaceMap.put("%totalplaytime%", FormatUtils.formatTimeAmount(""+data.getTotalPlayTime()));
        replaceMap.put("%ops%", ""+data.getOps());
        replaceMap.put("%refresh%", FormatUtils.formatTimeAmountSinceString(""+data.getRefreshDate(), new Date()));
        replaceMap.put("%totallogins%", ""+data.getTotalLoginTimes());
        return replaceMap;
    }

    static boolean isActive(long lastPlayed, long playTime, int loginTimes) {
        Plan plugin = getPlugin(Plan.class);
        int timeToActive = plugin.getConfig().getInt("Settings.Analysis.MinutesPlayedUntilConsidiredActive");
        long twoWeeks = 1209600;
        if (new Date().getTime() - lastPlayed < twoWeeks) {
            if (loginTimes > 3) {
                if (playTime > 60 * timeToActive) {
                    return true;
                }
            }
        }
        return false;
    }

    static String createActivityPieChart(int totalBanned, int active, int inactive) {
        String url = ActivityPieChartCreator.createChart(totalBanned, active, inactive);
        return "<img src=\"" + url + "\">";
    }
    
    static String createCommandUseListHtml(HashMap<String, Integer> commandUse) {
        List<String[]> sorted = MapComparator.sortByValue(commandUse);
        String html ="<table>";
        if (sorted.isEmpty()) {
            html = "<p>Error Calcuclating Command usages</p>";
            return html;
        }
        int i = 1;
        for (String[] values : sorted) {
            if (i >= 50) {
                break;
            }
            html += "<tr style=\"text-align: center;\"><td><b>"+values[1]+"</b></td>\r\n<td>"+values[0]+"</td></tr>";
            i++;
        }
        html += "</table>";
        return html;
    }
}
