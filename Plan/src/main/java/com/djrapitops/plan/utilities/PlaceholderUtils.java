package main.java.com.djrapitops.plan.utilities;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.ui.tables.SortabeSessionTableCreator;
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
        replaceMap.put("%commanduse%", data.getTop50CommandsListHtml());
        replaceMap.put("%avgage%", (data.getAverageAge() != -1) ? "" + data.getAverageAge() : Phrase.DEM_UNKNOWN + "");
        replaceMap.put("%avgplaytime%", FormatUtils.formatTimeAmount("" + data.getAveragePlayTime()));
        replaceMap.put("%totalplaytime%", FormatUtils.formatTimeAmount("" + data.getTotalPlayTime()));
        replaceMap.put("%ops%", "" + data.getOps());
        replaceMap.put("%refresh%", FormatUtils.formatTimeAmountSinceString("" + data.getRefreshDate(), new Date()));
        replaceMap.put("%totallogins%", "" + data.getTotalLoginTimes());
        replaceMap.put("%top20mostactive%", data.getTop20ActivePlayers());
        replaceMap.put("%recentlogins%", data.getRecentPlayers());
        replaceMap.put("%deaths%", data.getTotaldeaths() + "");
        replaceMap.put("%playerkills%", data.getTotalkills() + "");
        replaceMap.put("%mobkills%", data.getTotalmobkills() + "");
        Plan plugin = getPlugin(Plan.class);
        replaceMap.put("%version%", plugin.getDescription().getVersion());
        replaceMap.put("%planlite%", "");
        replaceMap.put("%sortabletable%", data.getSortablePlayersTable());
        replaceMap.putAll(plugin.getHookHandler().getAdditionalAnalysisReplaceRules());
        return replaceMap;
    }

    /**
     * Gets the HashMap that is used to replace placeholders.
     *
     * @param data UserData used to replace the placeholders with
     * @return HashMap that contains string for each placeholder.
     * @throws java.io.FileNotFoundException if planliteplayer.html is not found
     */
    public static HashMap<String, String> getInspectReplaceRules(UserData data) throws FileNotFoundException {
        HashMap<String, String> replaceMap = new HashMap<>();
        boolean showIPandUUID = Settings.SECURITY_IP_UUID.isTrue();
        UUID uuid = data.getUuid();
        replaceMap.put("%uuid%", (showIPandUUID ? "" + uuid : Html.HIDDEN.parse()));
        replaceMap.put("%lastseen%", FormatUtils.formatTimeStamp("" + data.getLastPlayed()));
        replaceMap.put("%logintimes%", "" + data.getLoginTimes());
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
        replaceMap.put("%ips%", (showIPandUUID ? data.getIps().toString() : Html.HIDDEN.parse()));
        replaceMap.put("%nicknames%", FormatUtils.swapColorsToSpan(data.getNicknames().toString()));
        replaceMap.put("%name%", data.getName());
        replaceMap.put("%registered%", FormatUtils.formatTimeStamp("" + data.getRegistered()));
        replaceMap.put("%timeskicked%", "" + data.getTimesKicked());
        replaceMap.put("%playtime%", FormatUtils.formatTimeAmount("" + data.getPlayTime()));
        replaceMap.put("%banned%", data.isBanned() ? Html.BANNED.parse() : "");
        replaceMap.put("%op%", data.isOp() ? Html.OPERATOR.parse() : "");
        replaceMap.put("%isonline%", (data.isOnline()) ? Html.ONLINE.parse() : Html.OFFLINE.parse());
        int deaths = data.getDeaths();
        replaceMap.put("%deaths%", deaths + "");
        replaceMap.put("%playerkills%", data.getPlayerKills().size() + "");
        replaceMap.put("%mobkills%", data.getMobKills() + "");
        replaceMap.put("%sessionstable%", SortabeSessionTableCreator.createSortedSessionDataTable5(data.getSessions()));
        Plan plugin = getPlugin(Plan.class);
        replaceMap.put("%version%", plugin.getDescription().getVersion());
        replaceMap.put("%planlite%", "");
        replaceMap.put("%inaccuratedatawarning%", (new Date().getTime() - data.getRegistered() < 180000) ? Html.WARN_INACCURATE.parse() : "");
        replaceMap.putAll(plugin.getHookHandler().getAdditionalInspectReplaceRules(uuid));
        return replaceMap;
    }
}
