package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.api.TimeAmount;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.analysis.GamemodePart;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.ui.html.graphs.PlayerActivityGraphCreator;
import main.java.com.djrapitops.plan.ui.html.graphs.PunchCardGraphCreator;
import main.java.com.djrapitops.plan.ui.html.graphs.SessionLengthDistributionGraphCreator;
import main.java.com.djrapitops.plan.ui.html.tables.KillsTableCreator;
import main.java.com.djrapitops.plan.ui.html.tables.SessionTableCreator;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    public static Map<String, String> getAnalysisReplaceRules(AnalysisData data) {
        Benchmark.start("Replace Placeholders Anaysis");
        HashMap<String, String> replaceMap = new HashMap<>();
        replaceMap.putAll(data.getReplaceMap());
        replaceMap.put("%plugins%", data.replacePluginsTabLayout());

        replaceMap.put("%currenttime%", MiscUtils.getTime() + "");
        replaceMap.put("%refresh%", FormatUtils.formatTimeAmountDifference(data.getRefreshDate(), MiscUtils.getTime()));
        replaceMap.put("%refreshlong%", data.getRefreshDate() + "");

        replaceMap.put("%graphmaxplayers%", Settings.GRAPH_PLAYERS_USEMAXPLAYERS_SCALE.isTrue() ? Plan.getInstance().getVariable().getMaxPlayers() + "" : "2");
        replaceMap.put("%servername%", Settings.SERVER_NAME.toString());

        // Html Theme colors
        String[] colors = new String[]{Settings.HCOLOR_MAIN.toString(), Settings.HCOLOR_MAIN_DARK.toString(), Settings.HCOLOR_SEC.toString(), Settings.HCOLOR_TER.toString(), Settings.HCOLOR_TER_DARK.toString()};
        String[] defaultCols = new String[]{"348e0f", "267F00", "5cb239", "89c471", "5da341"};
        for (int i = 0; i < colors.length; i++) {
            if (!defaultCols[i].equals(colors[i])) {
                replaceMap.put("#" + defaultCols[i], "#" + colors[i]);
            }
        }
        Benchmark.stop("Replace Placeholders Anaysis");
        return replaceMap;
    }

    /**
     * Gets the HashMap that is used to replace placeholders.
     *
     * @param data UserData used to replace the placeholders with
     * @return HashMap that contains string for each placeholder.
     * @throws java.io.FileNotFoundException if planliteplayer.html is not found
     */
    public static Map<String, String> getInspectReplaceRules(UserData data) throws FileNotFoundException {
        Benchmark.start("Replace Placeholders Inspect");

        HashMap<String, String> replaceMap = new HashMap<>();
        boolean showIPandUUID = Settings.SECURITY_IP_UUID.isTrue();
        UUID uuid = data.getUuid();
        replaceMap.put("%uuid%", (showIPandUUID ? "" + uuid : Html.HIDDEN.parse()));
        replaceMap.put("%lastseen%", FormatUtils.formatTimeStampYear(data.getLastPlayed()));
        replaceMap.put("%logintimes%", "" + data.getLoginTimes());
        replaceMap.put("%geoloc%", data.getGeolocation());
        long now = MiscUtils.getTime();
        boolean isActive = AnalysisUtils.isActive(now, data.getLastPlayed(), data.getPlayTime(), data.getLoginTimes());
        replaceMap.put("%active%", isActive ? Html.ACTIVE.parse() : Html.INACTIVE.parse());
        GamemodePart gmPart = new GamemodePart(null);
        Map<String, Long> gmTimes = data.getGmTimes();
        String[] gms = new String[]{"SURVIVAL", "CREATIVE", "ADVENTURE", "SPECTATOR"};
        for (String gm : gms) {
            Long time = gmTimes.get(gm);
            if (time != null) {
                gmPart.addTo(gm, time);
            }
        }
        gmPart.analyse();
        replaceMap.putAll(gmPart.getReplaceMap());

        replaceMap.put("%ips%", (showIPandUUID ? data.getIps().toString() : Html.HIDDEN.parse()));
        replaceMap.put("%nicknames%", HtmlUtils.removeXSS(HtmlUtils.swapColorsToSpan(data.getNicknames().toString())));
        replaceMap.put("%name%", data.getName());
        replaceMap.put("%registered%", FormatUtils.formatTimeStampYear(data.getRegistered()));
        replaceMap.put("%timeskicked%", "" + data.getTimesKicked());
        replaceMap.put("%playtime%", FormatUtils.formatTimeAmount(data.getPlayTime()));
        replaceMap.put("%banned%", data.isBanned() ? Html.BANNED.parse() : "");
        replaceMap.put("%op%", data.isOp() ? Html.OPERATOR.parse() : "");
        replaceMap.put("%isonline%", (data.isOnline()) ? Html.ONLINE.parse() : Html.OFFLINE.parse());
        replaceMap.put("%deaths%", data.getDeaths() + "");
        replaceMap.put("%playerkills%", data.getPlayerKills().size() + "");
        replaceMap.put("%mobkills%", data.getMobKills() + "");
        replaceMap.put("%sessionstable%", SessionTableCreator.createSortedSessionDataTable10(data.getSessions()));
        replaceMap.put("%sessionaverage%", FormatUtils.formatTimeAmount(MathUtils.averageLong(AnalysisUtils.transformSessionDataToLengths(data.getSessions()))));
        replaceMap.put("%killstable%", KillsTableCreator.createSortedSessionDataTable10(data.getPlayerKills()));
        Plan plugin = Plan.getInstance();
        replaceMap.put("%version%", plugin.getDescription().getVersion());
        replaceMap.put("%planlite%", "");
        replaceMap.put("%graphmaxplayers%", 2 + "");
        String scatterGraphData = PlayerActivityGraphCreator.buildScatterDataStringSessions(data.getSessions(), TimeAmount.WEEK.ms());
        replaceMap.put("%dataweek%", scatterGraphData);
        replaceMap.put("%playersgraphcolor%", Settings.HCOLOR_ACT_ONL + "");
        replaceMap.put("%playersgraphfill%", Settings.HCOLOR_ACT_ONL_FILL + "");
        replaceMap.put("%datapunchcard%", PunchCardGraphCreator.generateDataArray(data.getSessions()));
        String[] distribution = SessionLengthDistributionGraphCreator.generateDataArraySessions(data.getSessions());
        replaceMap.put("%datasessiondistribution%", distribution[0]);
        replaceMap.put("%labelssessiondistribution%", distribution[1]);
        replaceMap.put("%inaccuratedatawarning%", (now - data.getRegistered() < 180000) ? Html.WARN_INACCURATE.parse() : "");
        String[] colors = new String[]{Settings.HCOLOR_MAIN.toString(), Settings.HCOLOR_MAIN_DARK.toString(), Settings.HCOLOR_SEC.toString(), Settings.HCOLOR_TER.toString(), Settings.HCOLOR_TER_DARK.toString()};
        String[] defaultCols = new String[]{"348e0f", "267F00", "5cb239", "89c471", "5da341"};
        for (int i = 0; i < colors.length; i++) {
            if (!defaultCols[i].equals(colors[i])) {
                replaceMap.put("#" + defaultCols[i], "#" + colors[i]);
            }
        }
        replaceMap.put("%refreshlong%", plugin.getInspectCache().getCacheTime(uuid) + "");
        replaceMap.put("%currenttime%", MiscUtils.getTime() + "");
        replaceMap.put("%servername%", Settings.SERVER_NAME.toString());
        String pluginsTabHtml = plugin.getHookHandler().getPluginsTabLayoutForInspect();
        Map<String, String> additionalReplaceRules = plugin.getHookHandler().getAdditionalInspectReplaceRules(uuid);
        String replacedOnce = HtmlUtils.replacePlaceholders(pluginsTabHtml, additionalReplaceRules);
        replaceMap.put("%plugins%", HtmlUtils.replacePlaceholders(replacedOnce, additionalReplaceRules));
        Benchmark.stop("Replace Placeholders Inspect");
        return replaceMap;
    }
}
