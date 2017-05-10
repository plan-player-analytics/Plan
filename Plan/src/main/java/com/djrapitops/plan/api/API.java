package main.java.com.djrapitops.plan.api;

import java.util.Date;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.DataRequestHandler;
import main.java.com.djrapitops.plan.ui.webserver.WebSocketServer;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.UUIDFetcher;
import org.bukkit.OfflinePlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;

/**
 * This class contains the API methods.
 * <p>
 * Revamp incoming in 3.1.0
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class API {

    private Plan plugin;

    /**
     * Class Construcor.
     *
     * @param plugin Current instance of Plan
     */
    public API(Plan plugin) {
        this.plugin = plugin;
    }

    public void addPluginDataSource(PluginData dataSource) {
        plugin.getHookHandler().addPluginDataSource(dataSource);
    }
    
    public String getPlayerInspectPageLinkHtml(UUID uuid) throws IllegalStateException {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        if (offlinePlayer.hasPlayedBefore()) {
            return HtmlUtils.getInspectUrl(offlinePlayer.getName());
        }
        throw new IllegalStateException("Player has not played on this server before.");
    }
    
    /**
     * Uses UUIDFetcher to turn PlayerName to UUID
     *
     * @param playerName Player's name
     * @return UUID of the Player
     * @throws Exception if player's name is not registered at Mojang
     */
    public UUID playerNameToUUID(String playerName) throws Exception {
        return UUIDFetcher.getUUIDOf(playerName);
    }
    
    
    // DEPRECATED METHODS WILL BE REMOVED IN 3.2.0
    @Deprecated
    public static String formatTimeSinceDate(Date before, Date after) {
        return FormatUtils.formatTimeAmountSinceDate(before, after);
    }

    @Deprecated
    public static String formatTimeSinceString(String before, Date after) {
        return FormatUtils.formatTimeAmountSinceString(before, after);
    }

    @Deprecated
    public static String formatTimeAmount(String timeInMs) {
        return FormatUtils.formatTimeAmount(timeInMs);
    }

    @Deprecated
    public static String formatTimeStamp(String timeInMs) {
        return FormatUtils.formatTimeStamp(timeInMs);
    }

    @Deprecated
    public void cacheUserDataToInspectCache(UUID uuid) {
        plugin.getInspectCache().cache(uuid);
    }

    @Deprecated
    public String getPlayerHtmlAsString(UUID uuid) {
        WebSocketServer server = plugin.getUiServer();
        if (server != null) {
            return server.getDataReqHandler().getInspectHtml(uuid);
        }
        DataRequestHandler reqH = new DataRequestHandler(plugin);
        return reqH.getInspectHtml(uuid);
    }

    @Deprecated
    public void updateAnalysisCache() {
        plugin.getAnalysisCache().updateCache();
    }

    @Deprecated
    public String getAnalysisHtmlAsString() {
        WebSocketServer server = plugin.getUiServer();
        if (server != null) {
            return server.getDataReqHandler().getAnalysisHtml();
        }
        DataRequestHandler reqH = new DataRequestHandler(plugin);
        return reqH.getAnalysisHtml();
    }

    @Deprecated
    public UserData getUserDataFromInspectCache(UUID uuid) {
        return plugin.getInspectCache().getFromCache(uuid);
    }
    
    @Deprecated
    public AnalysisData getAnalysisDataFromCache() {
        return plugin.getAnalysisCache().getData();
    }
}
