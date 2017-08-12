package main.java.com.djrapitops.plan.utilities;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.analysis.GamemodePart;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.database.tables.GMTimesTable;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.ui.html.graphs.PlayerActivityGraphCreator;
import main.java.com.djrapitops.plan.ui.html.graphs.PunchCardGraphCreator;
import main.java.com.djrapitops.plan.ui.html.graphs.SessionLengthDistributionGraphCreator;
import main.java.com.djrapitops.plan.ui.html.graphs.WorldPieCreator;
import main.java.com.djrapitops.plan.ui.html.tables.KillsTableCreator;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author Rsl1122
 */
public class PlaceholderUtils {

    /**
     * Constructor used to hide the public constructor
     */
    private PlaceholderUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets the Map that is used to replace placeholders in Analysis.
     *
     * @param data AnalysisData used to replace the placeholders with
     * @return HashMap that contains string for each placeholder.
     */
    public static Map<String, Serializable> getAnalysisReplaceRules(AnalysisData data) {
        HashMap<String, Serializable> replaceMap = new HashMap<>();
        replaceMap.putAll(data.getReplaceMap());
        replaceMap.put("%plugins%", data.replacePluginsTabLayout());

        replaceMap.put("%refresh%", FormatUtils.formatTimeAmountDifference(data.getRefreshDate(), MiscUtils.getTime()));
        replaceMap.put("%refreshlong%", String.valueOf(data.getRefreshDate()));

        replaceMap.put("%servername%", Settings.SERVER_NAME.toString());

        replaceMap.put("%timezone%", MiscUtils.getTimeZoneOffsetHours());

        // Html Theme colors
        String[] colors = new String[]{Settings.HCOLOR_MAIN.toString(), Settings.HCOLOR_MAIN_DARK.toString(), Settings.HCOLOR_SEC.toString(), Settings.HCOLOR_TER.toString(), Settings.HCOLOR_TER_DARK.toString()};
        String[] defaultCols = new String[]{"348e0f", "267F00", "5cb239", "89c471", "5da341"};
        for (int i = 0; i < colors.length; i++) {
            if (!defaultCols[i].equals(colors[i])) {
                replaceMap.put("#" + defaultCols[i], "#" + colors[i]);
            }
        }
        return replaceMap;
    }

    /**
     * Gets the Map that is used to replace placeholders.
     *
     * @param data UserData used to replace the placeholders with
     * @return HashMap that contains string for each placeholder.
     */
    public static Map<String, Serializable> getInspectReplaceRules(UserData data) {

        HashMap<String, Serializable> replaceMap = new HashMap<>();
        replaceMap.put("%timezone%", MiscUtils.getTimeZoneOffsetHours());

        boolean showIPandUUID = Settings.SECURITY_IP_UUID.isTrue();
        UUID uuid = data.getUuid();
        replaceMap.put("%uuid%", (showIPandUUID ? uuid.toString() : "Hidden (Config)"));
        replaceMap.put("%lastseen%", FormatUtils.formatTimeStampYear(data.getLastPlayed()));
        replaceMap.put("%logintimes%", data.getLoginTimes());
        replaceMap.put("%geoloc%", data.getGeolocation());
        long now = MiscUtils.getTime();
        boolean isActive = AnalysisUtils.isActive(now, data.getLastPlayed(), data.getPlayTime(), data.getLoginTimes());
        replaceMap.put("%active%", isActive ? Locale.get(Msg.HTML_ACTIVE).parse() : Locale.get(Msg.HTML_INACTIVE).parse());
        GamemodePart gmPart = new GamemodePart();
        Map<String, Long> gmTimes = data.getGmTimes().getTimes();
        String[] gms = GMTimesTable.getGMKeyArray();
        for (String gm : gms) {
            Long time = gmTimes.get(gm);
            if (time != null) {
                gmPart.addTo(gm, time);
            }
        }
        gmPart.analyse();
        replaceMap.putAll(gmPart.getReplaceMap());

        replaceMap.put("%ips%", showIPandUUID ? data.getIps().toString() : "Hidden (Config)");
        replaceMap.put("%nicknames%", HtmlUtils.removeXSS(HtmlUtils.swapColorsToSpan(data.getNicknames().toString())));
        replaceMap.put("%name%", data.getName());
        replaceMap.put("%registered%", FormatUtils.formatTimeStampYear(data.getRegistered()));
        replaceMap.put("%timeskicked%", "" + data.getTimesKicked());
        replaceMap.put("%playtime%", FormatUtils.formatTimeAmount(data.getPlayTime()));
        replaceMap.put("%banned%", data.isBanned() ? Locale.get(Msg.HTML_BANNED).parse() : "");
        replaceMap.put("%op%", data.isOp() ? Locale.get(Msg.HTML_OP).parse() : "");
        replaceMap.put("%isonline%", data.isOnline() ? Locale.get(Msg.HTML_ONLINE).parse() : Locale.get(Msg.HTML_OFFLINE).parse());
        replaceMap.put("%deaths%", data.getDeaths());
        replaceMap.put("%playerkills%", data.getPlayerKills().size());
        replaceMap.put("%mobkills%", data.getMobKills());
        replaceMap.put("%sessionaverage%", FormatUtils.formatTimeAmount(MathUtils.averageLong(AnalysisUtils.transformSessionDataToLengths(data.getSessions()))));
        replaceMap.put("%killstable%", KillsTableCreator.createKillsTable(data.getPlayerKills()));
        Plan plugin = Plan.getInstance();
        replaceMap.put("%version%", plugin.getDescription().getVersion());
        replaceMap.put("%playersgraphcolor%", Settings.HCOLOR_ACT_ONL.toString());

        Set<SessionData> sessions = new HashSet<>(data.getSessions());
        List<Long> lengths = AnalysisUtils.transformSessionDataToLengths(sessions);
        replaceMap.put("%punchcardseries%", PunchCardGraphCreator.createDataSeries(sessions));
        replaceMap.put("%sessionlengthseries%", SessionLengthDistributionGraphCreator.createDataSeries(lengths));
        replaceMap.put("%playersonlineseries%", PlayerActivityGraphCreator.buildSeriesDataStringSessions(sessions));
        WorldTimes worldTimes = data.getWorldTimes();
        replaceMap.put("%worldseries%", WorldPieCreator.createSeriesData(worldTimes.getTimes()));
        replaceMap.put("%worldtotal%", FormatUtils.formatTimeAmount(worldTimes.getTotal()));

        String[] colors = new String[]{Settings.HCOLOR_MAIN.toString(), Settings.HCOLOR_MAIN_DARK.toString(), Settings.HCOLOR_SEC.toString(), Settings.HCOLOR_TER.toString(), Settings.HCOLOR_TER_DARK.toString()};
        String[] defaultCols = new String[]{"348e0f", "267F00", "5cb239", "89c471", "5da341"};
        for (int i = 0; i < colors.length; i++) {
            if (!defaultCols[i].equals(colors[i])) {
                replaceMap.put("#" + defaultCols[i], "#" + colors[i]);
            }
        }
        long cacheTime = plugin.getInspectCache().getCacheTime(uuid);
        replaceMap.put("%refresh%", FormatUtils.formatTimeAmountDifference(cacheTime, now));
        replaceMap.put("%refreshlong%", cacheTime);
        replaceMap.put("%servername%", Settings.SERVER_NAME.toString());
        String pluginsTabHtml = plugin.getHookHandler().getPluginsTabLayoutForInspect();
        Map<String, Serializable> additionalReplaceRules = plugin.getHookHandler().getAdditionalInspectReplaceRules(uuid);
        String replacedOnce = HtmlUtils.replacePlaceholders(pluginsTabHtml, additionalReplaceRules);
        replaceMap.put("%plugins%", HtmlUtils.replacePlaceholders(replacedOnce, additionalReplaceRules));
        return replaceMap;
    }
}
