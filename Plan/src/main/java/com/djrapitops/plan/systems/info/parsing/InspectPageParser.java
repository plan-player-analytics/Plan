/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.parsing;

import com.djrapitops.plugin.api.TimeAmount;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.data.Action;
import main.java.com.djrapitops.plan.data.PlayerKill;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.SessionsTable;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.comparators.ActionComparator;
import main.java.com.djrapitops.plan.utilities.comparators.SessionLengthComparator;
import main.java.com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import main.java.com.djrapitops.plan.utilities.html.HtmlStructure;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.html.graphs.PunchCardGraphCreator;
import main.java.com.djrapitops.plan.utilities.html.tables.ActionsTableCreator;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class InspectPageParser {

    private final UUID uuid;
    private final IPlan plugin;

    private final Map<String, Serializable> placeHolders;

    public InspectPageParser(UUID uuid, IPlan plugin) {
        this.uuid = uuid;
        this.plugin = plugin;
        placeHolders = new HashMap<>();
    }

    public String parse() throws SQLException, FileNotFoundException {
        Log.debug("Database", "Inspect Parse Fetch");
        Benchmark.start("Inspect Parse, Fetch");
        Database db = plugin.getDB();
        UserInfo userInfo = db.getUserInfoTable().getUserInfo(uuid);
        int timesKicked = db.getUsersTable().getTimesKicked(uuid);

        addValue("version", MiscUtils.getPlanVersion());
        addValue("serverName", Settings.SERVER_NAME.toString());

        addValue("playerName", userInfo.getName());
        addValue("registered", FormatUtils.formatTimeStampYear(userInfo.getRegistered()));
        long lastSeen = userInfo.getLastSeen();
        addValue("lastSeen", FormatUtils.formatTimeStampYear(lastSeen));
        addValue("kickCount", timesKicked);

        SessionsTable sessionsTable = db.getSessionsTable();
        Map<String, Long> playtimeByServer = sessionsTable.getPlaytimeByServer(uuid);

        List<String> geolocations = db.getIpsTable().getGeolocations(uuid);
        List<String> nicknames = db.getNicknamesTable().getNicknames(uuid).stream()
                .map(HtmlUtils::swapColorsToSpan)
                .collect(Collectors.toList());

        addValue("nicknames", HtmlStructure.createDotList(nicknames.toArray(new String[nicknames.size()])));
        addValue("geolocations", HtmlStructure.createDotList(geolocations.toArray(new String[geolocations.size()])));

        Map<String, List<Session>> sessions = sessionsTable.getSessions(uuid);
        List<Session> allSessions = sessions.values().stream()
                .flatMap(Collection::stream)
                .sorted(new SessionStartComparator())
                .collect(Collectors.toList());

        addValue("contentSessions", HtmlStructure.createSessionsTabContent(sessions, allSessions));
        addValue("contentServerOverview", HtmlStructure.createServerOverviewColumn(sessions));

        long now = MiscUtils.getTime();
        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();

        List<Session> sessionsDay = allSessions.stream()
                .filter(s -> s.getSessionStart() > dayAgo)
                .sorted(new SessionLengthComparator())
                .collect(Collectors.toList());
        List<Session> sessionsWeek = allSessions.stream()
                .filter(s -> s.getSessionStart() > weekAgo)
                .sorted(new SessionLengthComparator())
                .collect(Collectors.toList());

        int sessionCountDay = sessionsDay.size();
        int sessionCountWeek = sessionsWeek.size();
        long playtimeDay = AnalysisUtils.getTotalPlaytime(sessionsDay);
        long playtimeWeek = AnalysisUtils.getTotalPlaytime(sessionsWeek);

        if (!sessionsDay.isEmpty()) {
            addValue("sessionLengthLongestDay", FormatUtils.formatTimeAmount(sessionsDay.get(0).getLength()));
        } else {
            addValue("sessionLengthLongestDay", "-");
        }
        if (!sessionsWeek.isEmpty()) {
            addValue("sessionLengthLongestWeek", FormatUtils.formatTimeAmount(sessionsWeek.get(0).getLength()));
        } else {
            addValue("sessionLengthLongestWeek", "-");
        }

        addValue("sessionCountDay", sessionCountDay);
        addValue("sessionCountWeek", sessionCountWeek);
        addValue("playtimeDay", FormatUtils.formatTimeAmount(playtimeDay));
        addValue("playtimeWeek", FormatUtils.formatTimeAmount(playtimeWeek));

        List<Action> actions = db.getActionsTable().getActions(uuid);
        actions.addAll(allSessions.stream()
                .map(Session::getPlayerKills)
                .flatMap(Collection::stream)
                .map(PlayerKill::convertToAction)
                .collect(Collectors.toList()));
        actions.sort(new ActionComparator());

        addValue("tableBodyActions", ActionsTableCreator.createTableContent(actions));

        Benchmark.stop("Inspect Parse, Fetch");

        long playTime = AnalysisUtils.getTotalPlaytime(allSessions);
        int sessionCount = allSessions.size();

        addValue("sessionCount", sessionCount);
        addValue("playtimeTotal", FormatUtils.formatTimeAmount(playTime));
        addValue("lastSeen", FormatUtils.formatTimeAmount(playTime));

        String puchCardData = PunchCardGraphCreator.createDataSeries(allSessions);
        List<Session> sessionsInLengthOrder = allSessions.stream()
                .sorted(new SessionLengthComparator())
                .collect(Collectors.toList());
        if (sessionsInLengthOrder.isEmpty()) {
            addValue("sessionLengthMedian", "-");
            addValue("sessionLengthLongest", "-");
        } else {
            Session medianSession = sessionsInLengthOrder.get(sessionsInLengthOrder.size() / 2);
            addValue("sessionLengthMedian", FormatUtils.formatTimeAmount(medianSession.getLength()));
            addValue("sessionLengthLongest", FormatUtils.formatTimeAmount(sessionsInLengthOrder.get(0).getLength()));
        }

        long playerKillCount = allSessions.stream().map(Session::getPlayerKills).mapToLong(Collection::size).sum();
        long mobKillCount = allSessions.stream().mapToLong(Session::getMobKills).sum();
        long deathCount = allSessions.stream().mapToLong(Session::getDeaths).sum();

        addValue("playerKillCount", playerKillCount);
        addValue("mobKillCount", mobKillCount);
        addValue("deathCount", deathCount);


        playerClassification(userInfo, lastSeen, playTime, sessionCount);

        return HtmlUtils.replacePlaceholders(FileUtil.getStringFromResource("player.html"), placeHolders);
    }

    private void playerClassification(UserInfo userInfo, long lastPlayed, long playTime, int loginTimes) {
        boolean isBanned = userInfo.isBanned();
        boolean isOP = userInfo.isOpped();
        boolean isActive = AnalysisUtils.isActive(MiscUtils.getTime(), lastPlayed, playTime, loginTimes);

        String active = isActive ? "Active" : "Inactive";
        String banned = isBanned ? "Banned" : "";
        String op = isOP ? "Operator (OP)" : "";

        addValue("playerClassification", HtmlStructure.separateWithDots(active, banned, op));
    }

    private void addValue(String placeholder, Serializable value) {
        placeHolders.put(placeholder, value);
    }
}