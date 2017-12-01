/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.parsing;

import com.djrapitops.plugin.api.TimeAmount;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.api.exceptions.ParseException;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.settings.Settings;
import main.java.com.djrapitops.plan.settings.theme.ThemeVal;
import main.java.com.djrapitops.plan.settings.theme.Theme;
import main.java.com.djrapitops.plan.systems.info.BungeeInformationManager;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import main.java.com.djrapitops.plan.utilities.html.HtmlStructure;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.html.graphs.line.PlayerActivityGraphCreator;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Html String parser for /network page.
 *
 * @author Rsl1122
 */
public class NetworkPageParser extends PageParser {

    private final PlanBungee plugin;

    public NetworkPageParser(PlanBungee plugin) {
        this.plugin = plugin;
    }

    @Override
    public String parse() throws ParseException {
        try {
            UUID serverUUID = plugin.getServerUuid();
            long now = MiscUtils.getTime();
            Database db = plugin.getDB();
            List<TPS> networkOnlineData = db.getTpsTable().getNetworkOnlineData();

            peakTimes(serverUUID, now, db);

            uniquePlayers(now, db);

            addValue("timeZone", MiscUtils.getTimeZoneOffsetHours());
            addValue("networkName", Settings.BUNGEE_NETWORK_NAME.toString());
            addValue("version", plugin.getVersion());
            addValue("playersOnlineSeries", PlayerActivityGraphCreator.buildSeriesDataString(networkOnlineData));
            addValue("playersGraphColor", Theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
            addValue("playersOnline", plugin.getProxy().getOnlineCount());

            addValue("playersTotal", db.getUsersTable().getPlayerCount());

            List<Long> registerDates = db.getUsersTable().getRegisterDates();
            addValue("playersNewDay", AnalysisUtils.getNewPlayers(registerDates, TimeAmount.DAY.ms(), now));
            addValue("playersNewWeek", AnalysisUtils.getNewPlayers(registerDates, TimeAmount.WEEK.ms(), now));
            addValue("playersNewMonth", AnalysisUtils.getNewPlayers(registerDates, TimeAmount.MONTH.ms(), now));

            Map<UUID, String> networkPageContents = ((BungeeInformationManager) plugin.getInfoManager()).getNetworkPageContent();
            addValue("tabContentServers", HtmlStructure.createNetworkPageContent(networkPageContents));

            return HtmlUtils.replacePlaceholders(FileUtil.getStringFromResource("web/network.html"), placeHolders);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    private void uniquePlayers(long now, Database db) throws SQLException {
        Map<UUID, Map<UUID, List<Session>>> allSessions = db.getSessionsTable().getAllSessions(false);
        Map<UUID, List<Session>> userSessions = AnalysisUtils.sortSessionsByUser(allSessions);

        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();
        long monthAgo = now - TimeAmount.MONTH.ms();

        addValue("playersUniqueDay", AnalysisUtils.getUniqueJoinsPerDay(userSessions, dayAgo));
        addValue("playersUniqueWeek", AnalysisUtils.getUniqueJoinsPerDay(userSessions, weekAgo));
        addValue("playersUniqueMonth", AnalysisUtils.getUniqueJoinsPerDay(userSessions, monthAgo));
    }

    private void peakTimes(UUID serverUUID, long now, Database db) throws SQLException {
        Optional<TPS> allTimePeak = db.getTpsTable().getAllTimePeak(serverUUID);
        Optional<TPS> lastPeak = db.getTpsTable().getPeakPlayerCount(serverUUID, now - TimeAmount.DAY.ms() * 2L);

        if (allTimePeak.isPresent()) {
            TPS tps = allTimePeak.get();
            addValue("bestPeakTime", FormatUtils.formatTimeStampYear(tps.getDate()));
            addValue("playersBestPeak", FormatUtils.formatTimeStampYear(tps.getPlayers()));
        } else {
            addValue("bestPeakTime", "No Data");
            addValue("playersBestPeak", "");
        }
        if (lastPeak.isPresent()) {
            TPS tps = lastPeak.get();
            addValue("lastPeakTime", FormatUtils.formatTimeStampYear(tps.getDate()));
            addValue("playersLastPeak", FormatUtils.formatTimeStampYear(tps.getPlayers()));
        } else {
            addValue("lastPeakTime", "No Data");
            addValue("playersLastPeak", "");
        }
    }
}