/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.info.parsing;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.systems.info.BungeeInformationManager;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plan.utilities.html.graphs.line.PlayerActivityGraph;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Html String parser for /network page.
 *
 * @author Rsl1122
 */
public class NetworkPage extends Page {

    private final PlanBungee plugin;

    public NetworkPage(PlanBungee plugin) {
        this.plugin = plugin;
    }

    @Override
    public String toHtml() throws ParseException {
        try {
            UUID serverUUID = plugin.getServerUuid();
            long now = MiscUtils.getTime();
            Database db = plugin.getDB();
            List<TPS> networkOnlineData = db.fetch().getNetworkOnlineData();

            peakTimes(serverUUID, now, db);

            uniquePlayers(now, db);

            addValue("timeZone", MiscUtils.getTimeZoneOffsetHours());
            addValue("networkName", Settings.BUNGEE_NETWORK_NAME.toString());
            addValue("version", plugin.getVersion());
            addValue("playersOnlineSeries", PlayerActivityGraph.createSeries(networkOnlineData));
            addValue("playersGraphColor", Theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
            addValue("playersOnline", plugin.getProxy().getOnlineCount());

            addValue("playersTotal", db.count().getNetworkPlayerCount());

            List<Long> registerDates = db.fetch().getRegisterDates();
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

    private void uniquePlayers(long now, Database db) throws DBException {
        Map<UUID, Map<UUID, List<Session>>> allSessions = db.fetch().getSessionsWithNoExtras();
        Map<UUID, List<Session>> userSessions = AnalysisUtils.sortSessionsByUser(allSessions);

        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();
        long monthAgo = now - TimeAmount.MONTH.ms();

        addValue("playersUniqueDay", AnalysisUtils.getUniqueJoinsPerDay(userSessions, dayAgo));
        addValue("playersUniqueWeek", AnalysisUtils.getUniqueJoinsPerDay(userSessions, weekAgo));
        addValue("playersUniqueMonth", AnalysisUtils.getUniqueJoinsPerDay(userSessions, monthAgo));
    }

    private void peakTimes(UUID serverUUID, long now, Database db) throws DBException {
        Optional<TPS> allTimePeak = db.fetch().getAllTimePeak(serverUUID);
        Optional<TPS> lastPeak = db.fetch().getPeakPlayerCount(serverUUID, now - TimeAmount.DAY.ms() * 2L);

        if (allTimePeak.isPresent()) {
            TPS tps = allTimePeak.get();
            addValue("bestPeakTime", FormatUtils.formatTimeStampYear(tps.getDate()));
            addValue("playersBestPeak", tps.getPlayers());
        } else {
            addValue("bestPeakTime", "No Data");
            addValue("playersBestPeak", "");
        }
        if (lastPeak.isPresent()) {
            TPS tps = lastPeak.get();
            addValue("lastPeakTime", FormatUtils.formatTimeStampYear(tps.getDate()));
            addValue("playersLastPeak", tps.getPlayers());
        } else {
            addValue("lastPeakTime", "No Data");
            addValue("playersLastPeak", "");
        }
    }
}