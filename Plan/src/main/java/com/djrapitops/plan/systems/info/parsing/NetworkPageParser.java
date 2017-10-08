/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.parsing;

import com.djrapitops.plugin.api.TimeAmount;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.exceptions.ParseException;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.systems.info.BungeeInformationManager;
import main.java.com.djrapitops.plan.systems.webserver.theme.Colors;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import main.java.com.djrapitops.plan.utilities.html.HtmlStructure;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.html.graphs.PlayerActivityGraphCreator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
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
            long now = MiscUtils.getTime();
            Database db = plugin.getDB();
            List<TPS> networkOnlineData = db.getTpsTable().getNetworkOnlineData();

            addValue("networkName", Settings.BUNGEE_NETWORK_NAME.toString());
            addValue("version", plugin.getVersion());
            addValue("playersOnlineSeries", PlayerActivityGraphCreator.buildSeriesDataString(networkOnlineData));
            addValue("playersGraphColor", Colors.PLAYERS_ONLINE.getColor());
            addValue("playersOnline", plugin.getProxy().getOnlineCount());
            addValue("playersMax", db.getServerTable().getMaxPlayers());

            addValue("playersTotal", db.getUsersTable().getPlayerCount());

            List<Long> registerDates = db.getUsersTable().getRegisterDates();
            addValue("playersNewDay", AnalysisUtils.getNewPlayers(registerDates, TimeAmount.DAY.ms(), now));
            addValue("playersNewWeek", AnalysisUtils.getNewPlayers(registerDates, TimeAmount.WEEK.ms(), now));

            Map<UUID, String> networkPageContents = ((BungeeInformationManager) plugin.getInfoManager()).getNetworkPageContent();
            addValue("contentServers", HtmlStructure.createNetworkPageContent(networkPageContents));

            return HtmlUtils.replacePlaceholders(FileUtil.getStringFromResource("network.html"), placeHolders);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
}