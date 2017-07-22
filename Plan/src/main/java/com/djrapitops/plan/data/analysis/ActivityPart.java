package main.java.com.djrapitops.plan.data.analysis;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.utilities.Verify;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.ui.html.RecentPlayersButtonsCreator;
import main.java.com.djrapitops.plan.ui.html.graphs.PlayerActivityGraphCreator;
import main.java.com.djrapitops.plan.ui.html.graphs.PunchCardGraphCreator;
import main.java.com.djrapitops.plan.ui.html.graphs.SessionLengthDistributionGraphCreator;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

/**
 * Part responsible for all Player Activity related analysis.
 *
 * Online Graphs, Player-base pie-chart, Recent Players and Session
 * visualisation.
 *
 * Placeholder values can be retrieved using the get method.
 *
 * Contains following place-holders: recentlogins, sessionaverage,
 * datapunchcard, datasessiondistribution, labelssessiondistribution,
 * datascatterday, datascatterweek, datascattermonth, playersonlinecolor,
 * playersgraphfill, activecol, inactivecol, joinleavecol, bannedcol,
 * activitycolors, labelsactivity, dataaactivity, active, inactive, joinleaver,
 * banned
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class ActivityPart extends RawData<ActivityPart> {

    private final JoinInfoPart joins;
    private final TPSPart tpsPart;

    private List<String> recentPlayers;
    private List<UUID> recentPlayersUUIDs;

    private final Set<UUID> bans;
    private final Set<UUID> active;
    private final Set<UUID> inactive;
    private final Set<UUID> joinedOnce;

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

        addValue("recentlogins", RecentPlayersButtonsCreator.createRecentLoginsButtons(recentPlayers, 15));

        activityPiechart();

        playerActivityGraphs();

        final List<SessionData> sessions = joins.getAllSessions();

        long averageLength = MathUtils.averageLong(AnalysisUtils.transformSessionDataToLengths(sessions));
        addValue("sessionaverage", FormatUtils.formatTimeAmount(averageLength));

        String punchCardArray = PunchCardGraphCreator.generateDataArray(sessions);
        addValue("datapunchcard", punchCardArray);

        String[] sessionDistribution = SessionLengthDistributionGraphCreator.generateDataArraySessions(sessions);
        addValue("datasessiondistribution", sessionDistribution[0]);
        addValue("labelssessiondistribution", sessionDistribution[1]);
    }

    private void playerActivityGraphs() {
        List<TPS> tpsData = tpsPart.getTpsData();

        String dayScatter = PlayerActivityGraphCreator.buildScatterDataString(tpsData, TimeAmount.DAY.ms());
        String weekScatter = PlayerActivityGraphCreator.buildScatterDataString(tpsData, TimeAmount.WEEK.ms());
        String monthScatter = PlayerActivityGraphCreator.buildScatterDataString(tpsData, TimeAmount.MONTH.ms());

        addValue("datascatterday", dayScatter);
        addValue("datascatterweek", weekScatter);
        addValue("datascattermonth", monthScatter);

        addValue("%playersgraphcolor%", Settings.HCOLOR_ACT_ONL + "");
        addValue("%playersgraphfill%", Settings.HCOLOR_ACT_ONL_FILL + "");
    }

    private void activityPiechart() {
        int[] counts = new int[]{active.size(), inactive.size(), joinedOnce.size(), bans.size()};
        final String colAct = Settings.HCOLOR_ACTP_ACT + "";
        final String colIna = Settings.HCOLOR_ACTP_INA + "";
        final String colJoi = Settings.HCOLOR_ACTP_JON + "";
        final String colBan = Settings.HCOLOR_ACTP_BAN + "";

        addValue("%activecol%", colAct);
        addValue("%inactivecol%", colIna);
        addValue("%joinleavecol%", colJoi);
        addValue("%bancol%", colBan);
        String activityColors = HtmlUtils.separateWithQuotes(
                "#" + colAct, "#" + colIna, "#" + colJoi, "#" + colBan
        );
        addValue("%activitycolors%", activityColors);

        String activityLabels = "[" + HtmlUtils.separateWithQuotes(
                Html.GRAPH_ACTIVE.parse(), Html.GRAPH_INACTIVE.parse(),
                Html.GRAPH_UNKNOWN.parse(), Html.GRAPH_BANNED.parse()) + "]";
        addValue("%labelsactivity%", activityLabels);

        addValue("activitydata", Arrays.toString(counts));
        addValue("%active%", counts[0]);
        addValue("%inactive%", counts[1]);
        addValue("%joinleaver%", counts[2]);
        addValue("%banned%", counts[3]);
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

    public void setRecentPlayers(List<String> recentPlayers) {
        this.recentPlayers = recentPlayers;
    }

    public void setRecentPlayersUUIDs(List<UUID> recentPlayersUUIDs) {
        this.recentPlayersUUIDs = recentPlayersUUIDs;
    }

    public Map<Long, Integer> getPlayersOnline() {
        return tpsPart.getTpsData().stream().collect(Collectors.toMap(TPS::getDate, TPS::getPlayers));
    }

    public List<String> getRecentPlayers() {
        return recentPlayers;
    }

    public List<UUID> getRecentPlayersUUIDs() {
        return recentPlayersUUIDs;
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
