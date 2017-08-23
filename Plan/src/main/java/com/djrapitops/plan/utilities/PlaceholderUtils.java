package main.java.com.djrapitops.plan.utilities;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.UserInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        replaceMap.put("tabContentPlugins", data.replacePluginsTabLayout());

        // TODO Refresh time for Network pages
//        replaceMap.put("refresh", FormatUtils.formatTimeAmountDifference(data.getRefreshDate(), MiscUtils.getDate()));
//        replaceMap.put("refreshlong", String.valueOf(data.getRefreshDate()));

        replaceMap.put("serverName", Settings.SERVER_NAME.toString());

        replaceMap.put("timeZone", MiscUtils.getTimeZoneOffsetHours());
        // TODO Add Theme Replace somewhere when getting
        return replaceMap;
    }

    /**
     * Gets the Map that is used to replace placeholders.
     *
     * @param data UserInfo used to replace the placeholders with
     * @return HashMap that contains string for each placeholder.
     */
    public static Map<String, Serializable> getInspectReplaceRules(UserInfo data) {

        HashMap<String, Serializable> replaceMap = new HashMap<>();
        UUID uuid = data.getUuid();
        replaceMap.put("playerName", data.getName());
        replaceMap.put("serverName", Settings.SERVER_NAME.toString());
        Plan plugin = Plan.getInstance();
        replaceMap.put("version", plugin.getDescription().getVersion());

        replaceMap.put("playerClassification", "TODO"); //TODO Playerclassification (Active &#x2022; Offline etc)
        replaceMap.put("nicknames", "TODO"); //TODO Nickname list creator &#x2022; name<br>
        replaceMap.put("geolocations", "TODO"); //TODO Geolocation list creator &#x2022; name<br>

        replaceMap.put("registered", FormatUtils.formatTimeStampYear(data.getRegistered()));
//        replaceMap.put("lastSeen", FormatUtils.formatTimeStampYear(data.getLastPlayed()));

//        replaceMap.put("sessionCount", data.getSessions().size());
        //TODO replaceMap.put("playtimeTotal", FormatUtils.formatTimeAmount(data.getPlayTime()));

//        replaceMap.put("kickCount", data.getTimesKicked());
//        replaceMap.put("playerKillCount", data.getPlayerKills().size()); //TODO
//        replaceMap.put("mobKillCount", data.getMobKills());
//        replaceMap.put("deathCount", data.getDeaths());

//        Set<Session> sessions = new HashSet<>(data.getSessions());
//        replaceMap.put("punchCardSeries", PunchCardGraphCreator.createDataSeries(sessions));
        //TODO    WorldTimes worldTimes = data.getWorldTimes();
//    TODO    replaceMap.put("worldSeries", WorldPieCreator.createSeriesData(worldTimes.getTimes()));
//        replaceMap.put("worldTotal", FormatUtils.formatTimeAmount(worldTimes.getTotal()));

        //TODO Plugin Tab content Web API
        //TODO Player Plugin tab code.
        String pluginsTabHtml = plugin.getHookHandler().getPluginsTabLayoutForInspect();
        Map<String, Serializable> additionalReplaceRules = plugin.getHookHandler().getAdditionalInspectReplaceRules(uuid);
        String replacedOnce = HtmlUtils.replacePlaceholders(pluginsTabHtml, additionalReplaceRules);
        replaceMap.put("tabContentPlugins", HtmlUtils.replacePlaceholders(replacedOnce, additionalReplaceRules));
        return replaceMap;
    }
}
