/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.parsing;

import com.djrapitops.plugin.api.TimeAmount;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.ParseException;
import main.java.com.djrapitops.plan.data.Action;
import main.java.com.djrapitops.plan.data.PlayerKill;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.SessionsTable;
import main.java.com.djrapitops.plan.database.tables.UsersTable;
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
import main.java.com.djrapitops.plan.utilities.html.graphs.ServerPreferencePieCreator;
import main.java.com.djrapitops.plan.utilities.html.graphs.WorldPieCreator;
import main.java.com.djrapitops.plan.utilities.html.tables.ActionsTableCreator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Used for parsing Inspect page out of database data and the html.
 *
 * @author Rsl1122
 */
public class InspectPageParser extends PageParser {

    private final UUID uuid;
    private final IPlan plugin;

    public InspectPageParser(UUID uuid, IPlan plugin) {
        this.uuid = uuid;
        this.plugin = plugin;
    }

    public String parse() throws ParseException {
        try {
            // TODO Player is online parts
            Log.debug("Database", "Inspect Parse Fetch");
            Benchmark.start("Inspect Parse, Fetch");
            Database db = plugin.getDB();
            SessionsTable sessionsTable = db.getSessionsTable();

            UserInfo userInfo = db.getUserInfoTable().getUserInfo(uuid);
            UsersTable usersTable = db.getUsersTable();
            String playerName = usersTable.getPlayerName(uuid);
            Optional<Long> registerDate = usersTable.getRegisterDate(uuid);
            if (registerDate.isPresent()) {
                addValue("registered", FormatUtils.formatTimeStampYear(registerDate.get()));
            } else {
                addValue("registered", "-");
            }

            addValue("playerName", playerName);
            int timesKicked = usersTable.getTimesKicked(uuid);

            addValue("version", MiscUtils.getPlanVersion());
            addValue("timeZone", MiscUtils.getTimeZoneOffsetHours());

            long lastSeen = sessionsTable.getLastSeen(uuid);
            if (lastSeen != 0) {
                addValue("lastSeen", FormatUtils.formatTimeStampYear(lastSeen));
            } else {
                addValue("lastSeen", "-");
            }
            addValue("kickCount", timesKicked);

            Map<String, Long> playtimeByServer = sessionsTable.getPlaytimeByServer(uuid);
            addValue("serverPieSeries", ServerPreferencePieCreator.createSeriesData(playtimeByServer));
            addValue("worldPieColors", Settings.THEME_GRAPH_WORLD_PIE.toString());
            addValue("gmPieColors", Settings.THEME_GRAPH_GM_PIE.toString());
            addValue("serverPieColors", Settings.THEME_GRAPH_SERVER_PREF_PIE.toString());

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

            String[] sessionsTabContent = HtmlStructure.createSessionsTabContent(sessions, allSessions);
            addValue("contentSessions", sessionsTabContent[0]);
            addValue("sessionTabGraphViewFunctions", sessionsTabContent[1]);
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

            addValue("sessionLengthLongestDay", !sessionsDay.isEmpty() ? FormatUtils.formatTimeAmount(sessionsDay.get(0).getLength()) : "-");
            addValue("sessionLengthLongestWeek", !sessionsWeek.isEmpty() ? FormatUtils.formatTimeAmount(sessionsWeek.get(0).getLength()) : "-");

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

            addValue("tableBodyActions", ActionsTableCreator.createTable(actions));

            Benchmark.stop("Inspect Parse, Fetch");

            long playTime = AnalysisUtils.getTotalPlaytime(allSessions);
            int sessionCount = allSessions.size();

            addValue("sessionCount", sessionCount);
            addValue("playtimeTotal", FormatUtils.formatTimeAmount(playTime));

            String punchCardData = PunchCardGraphCreator.createDataSeries(allSessions);
            String[] worldPieData = WorldPieCreator.createSeriesData(db.getWorldTimesTable().getWorldTimesOfUser(uuid));

            addValue("worldPieSeries", worldPieData[0]);
            addValue("gmSeries", worldPieData[1]);

            addValue("punchCardSeries", punchCardData);

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

            boolean isActive = AnalysisUtils.isActive(MiscUtils.getTime(), lastSeen, playTime, sessionCount);
            String active = isActive ? "Active" : "Inactive";
            if (userInfo != null) {
                playerClassification(userInfo, active);
            } else {
                addValue("playerClassification", active);
            }

            if (!plugin.getInfoManager().isUsingAnotherWebServer()) {
                addValue("networkName", Settings.SERVER_NAME.toString());
            }

            return HtmlUtils.replacePlaceholders(FileUtil.getStringFromResource("player.html"), placeHolders);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            throw new ParseException(e);
        }
    }

    private void playerClassification(UserInfo userInfo, String active) {
        boolean isBanned = userInfo.isBanned();
        boolean isOP = userInfo.isOpped();

        String banned = isBanned ? "Banned" : "";
        String op = isOP ? "Operator (OP)" : "";

        addValue("playerClassification", HtmlStructure.separateWithDots(active, banned, op));
    }

}
