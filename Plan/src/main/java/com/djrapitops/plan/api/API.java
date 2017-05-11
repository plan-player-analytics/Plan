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
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.OfflinePlayer;

/**
 * This class contains the API methods.
 * <p>
 * Methods can be called from Asyncronous task & are thread safe unless
 * otherwise stated.
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

    /**
     * Check whether or not the plugin enabled successfully.
     *
     * @return true if plugin is enabled correctly.
     */
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    /**
     * Add a source of plugin data to the Plugins tab on Analysis and/or Inspect
     * page.
     *
     * Refer to documentation on github or Javadoc of PluginData to set-up a
     * data source that extends PluginData correctly.
     *
     * @param dataSource an object that extends PluginData-object, thus allowing
     * Analysis & Inspect to manage the data of a plugin correctly.
     * @see PluginData
     */
    public void addPluginDataSource(PluginData dataSource) {
        if (isEnabled()) {
            plugin.getHookHandler().addPluginDataSource(dataSource);
        }
    }

    /**
     * Used to get the link to InspectPage of a player.
     *
     * This method is useful if you have a table and want to link to the inspect
     * page.
     *
     * Html.LINK.parse("Link", "Playername") can be used to get a link
     * {@code <a href="Link">Playername</a>}
     *
     * @param name Playername of the player
     * @return ip:port/security/player/Playername
     */
    public String getPlayerInspectPageLink(String name) {
        return HtmlUtils.getInspectUrl(name);
    }

    /**
     * Used to get the playerName of a player who has played on the server.
     *
     * @param uuid UUID of the player.
     * @return Playername, eg "Rsl1122"
     * @throws IllegalStateException If the player has not played on the server
     * before.
     */
    public String getPlayerName(UUID uuid) throws IllegalStateException {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        if (offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getName();
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
