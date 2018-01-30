/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.pages.parsing;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.pages.parts.NetworkPageContent;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plan.utilities.html.graphs.line.OnlineActivityGraph;
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

    @Override
    public String toHtml() throws ParseException {
        try {
            UUID serverUUID = ServerInfo.getServerUUID();
            long now = MiscUtils.getTime();
            Database database = Database.getActive();
            List<TPS> networkOnlineData = database.fetch().getNetworkOnlineData();

            peakTimes(serverUUID, now, database);

            uniquePlayers(now, database);

            addValue("timeZone", MiscUtils.getTimeZoneOffsetHours());
            addValue("networkName", Settings.BUNGEE_NETWORK_NAME.toString());
            addValue("version", VersionCheckSystem.getCurrentVersion());
            addValue("playersOnlineSeries", new OnlineActivityGraph(networkOnlineData).toHighChartsSeries());
            addValue("playersGraphColor", Theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
            addValue("playersOnline", ServerInfo.getServerProperties().getOnlinePlayers());

            addValue("playersTotal", database.count().getNetworkPlayerCount());

            List<Long> registerDates = database.fetch().getRegisterDates();
            addValue("playersNewDay", AnalysisUtils.getNewPlayers(registerDates, TimeAmount.DAY.ms(), now));
            addValue("playersNewWeek", AnalysisUtils.getNewPlayers(registerDates, TimeAmount.WEEK.ms(), now));
            addValue("playersNewMonth", AnalysisUtils.getNewPlayers(registerDates, TimeAmount.MONTH.ms(), now));

            NetworkPageContent networkPageContent = (NetworkPageContent)
                    ResponseCache.loadResponse(PageId.NETWORK_CONTENT.id(), NetworkPageContent::new);
            addValue("tabContentServers", networkPageContent.getContents());

            return HtmlUtils.replacePlaceholders(FileUtil.getStringFromResource("web/network.html"), placeHolders);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    private void uniquePlayers(long now, Database db) throws DBException {
        Map<UUID, Map<UUID, List<Session>>> allSessions = db.fetch().getSessionsInLastMonth();
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