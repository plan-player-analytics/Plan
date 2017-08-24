package main.java.com.djrapitops.plan.data.analysis;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.systems.webserver.theme.Colors;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.html.graphs.PlayerActivityGraphCreator;
import main.java.com.djrapitops.plan.utilities.html.graphs.PunchCardGraphCreator;
import main.java.com.djrapitops.plan.utilities.html.graphs.SessionLengthDistributionGraphCreator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Part responsible for all Player Activity related analysis.
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following placeholders after analyzed:
 * ${active} - (Number)
 * ${inactive} - (Number)
 * ${joinLeaver} - (Number)
 * ${banned} - (Number)
 * ${activityColors} - Color array
 * ${playersGraphColor} - Color
 * <p>
 * ${playersOnlineSeries} - Data for HighCharts
 * ${sessionLengthSeries} - Data for HighCharts
 * ${punchCardSeries} - Data for HighCharts
 * <p>
 * ${sessionAverage} - Formatted Time amount
 * //TODO ${tableBodyRecentLogins}
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class ActivityPart extends RawData {

    private final JoinInfoPart joins;
    private final TPSPart tpsPart;
    private final Set<UUID> bans;
    private final Set<UUID> active;
    private final Set<UUID> inactive;
    private final Set<UUID> joinedOnce;
    private List<String> recentPlayers;
    private List<UUID> recentPlayersUUIDs;

    public ActivityPart(JoinInfoPart joins, TPSPart tps) {
        this.joins = joins;
        tpsPart = tps;
        bans = new HashSet<>();
        active = new HashSet<>();
        inactive = new HashSet<>();
        joinedOnce = new HashSet<>();
    }

    @Override
    public void analyse() {
        Verify.nullCheck(recentPlayers);
        Verify.nullCheck(recentPlayersUUIDs);

        // TODO Recent logins table
        addValue("recentlogins", "");

        activityPiechart();

        playerActivityGraphs();

        final List<Session> sessions = joins.getAllSessions();

        List<Long> lengths = AnalysisUtils.transformSessionDataToLengths(sessions);
        long averageLength = MathUtils.averageLong(lengths);
        addValue("sessionAverage", FormatUtils.formatTimeAmount(averageLength));

        List<Session> sessionsMonth = sessions.stream()
                .filter(s -> s.getSessionStart() > MiscUtils.getTime() - TimeAmount.MONTH.ms())
                .collect(Collectors.toList());
        addValue("punchCardSeries", PunchCardGraphCreator.createDataSeries(sessionsMonth));
        addValue("sessionLengthSeries", SessionLengthDistributionGraphCreator.createDataSeries(lengths));
    }

    private void playerActivityGraphs() {
        List<TPS> tpsData = tpsPart.getTpsData();
        addValue("playersOnlineSeries", PlayerActivityGraphCreator.buildSeriesDataString(tpsData));
        addValue("playersGraphColor", Colors.PLAYERS_ONLINE.getColor());
    }

    private void activityPiechart() {
        int[] counts = new int[]{active.size(), inactive.size(), joinedOnce.size(), bans.size()};

        String activityColors = HtmlUtils.separateWithQuotes(
                "#55ffff", "#ff55ff", "#ff5555", "#ffff55" //TODO Write Colors (enums) for Activity pie.
        );
        addValue("activityColors", activityColors);

//        addValue("activitydata", Arrays.toString(counts)); // TODO Check if needed
        addValue("playersActive", counts[0]);
        addValue("active", counts[0]);
        addValue("inactive", counts[1]);
        addValue("joinLeaver", counts[2]);
        addValue("banned", counts[3]);
    }

    public void addBan(UUID uuid) {
        Verify.nullCheck(uuid);
        bans.add(uuid);
    }

    public void addActive(UUID uuid) {
        Verify.nullCheck(uuid);
        active.add(uuid);
    }

    public void addInActive(UUID uuid) {
        Verify.nullCheck(uuid);
        inactive.add(uuid);
    }

    public void addJoinedOnce(UUID uuid) {
        Verify.nullCheck(uuid);
        joinedOnce.add(uuid);
    }

    public Map<Long, Integer> getPlayersOnline() {
        return tpsPart.getTpsData().stream().collect(Collectors.toMap(TPS::getDate, TPS::getPlayers));
    }

    public List<String> getRecentPlayers() {
        return recentPlayers;
    }

    public void setRecentPlayers(List<String> recentPlayers) {
        this.recentPlayers = recentPlayers;
    }

    public List<UUID> getRecentPlayersUUIDs() {
        return recentPlayersUUIDs;
    }

    public void setRecentPlayersUUIDs(List<UUID> recentPlayersUUIDs) {
        this.recentPlayersUUIDs = recentPlayersUUIDs;
    }

    public Set<UUID> getBans() {
        return bans;
    }

    public Set<UUID> getActive() {
        return active;
    }

    public Set<UUID> getInactive() {
        return inactive;
    }

    public Set<UUID> getJoinedOnce() {
        return joinedOnce;
    }
}
