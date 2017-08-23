package main.java.com.djrapitops.plan.api;

import com.djrapitops.plugin.utilities.Verify;
import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.html.DataRequestHandler;
import main.java.com.djrapitops.plan.ui.webserver.WebServer;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;

import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

/**
 * This class contains the API methods.
 * <p>
 * Methods can be called from Asynchronous task and are thread safe unless
 * otherwise stated.
 * <p>
 * Use Plan.getPlanAPI() to get the API.
 * <p>
 * More information about API methods can be found on GitHub.
 *
 * @author Rsl1122
 * @see PluginData
 * @see AnalysisType
 * @since 2.0.0
 */
public class API {

    private final Plan plugin;

    /**
     * Class Constructor.
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
     * <p>
     * Refer to documentation on GitHub or Javadoc of PluginData to set-up a
     * data source that extends PluginData correctly.
     *
     * @param dataSource an object that extends PluginData-object, thus allowing
     *                   Analysis and Inspect to manage the data of a plugin correctly.
     * @see PluginData
     */
    public void addPluginDataSource(PluginData dataSource) {
        if (isEnabled()) {
            plugin.getHookHandler().addPluginDataSource(dataSource);
        }
    }

    /**
     * Used to get the link to InspectPage of a player.
     * <p>
     * This method is useful if you have a table and want to link to the inspect
     * page.
     * <p>
     * Html.LINK.parse("Link", "PlayerName") can be used to get a link
     * {@code <a href="Link">PlayerName</a>}
     *
     * @param name Name of the player
     * @return ip:port/security/player/PlayerName
     */
    public String getPlayerInspectPageLink(String name) {
        return HtmlUtils.getInspectUrlWithProtocol(name);
    }

    /**
     * Check if the UserInfo is cached to the InspectCache.
     *
     * @param uuid UUID of the player.
     * @return true/false
     */
    @Deprecated
    public boolean isPlayersDataInspectCached(UUID uuid) {
        // TODO Check PageCache
        return false;
    }

    /**
     * Cache the UserInfo to InspectCache.
     * <p>
     * Uses cache if data is cached or database if not. Call from an Asynchronous
     * thread.
     *
     * @param uuid UUID of the player.
     */
    @Deprecated
    public void cacheUserDataToInspectCache(UUID uuid) {
        // TODO Run Inspect parse
    }

    /**
     * Used to get the full Html of the Inspect page as a string.
     * <p>
     * Check if the data is cached to InspectCache before calling this.
     *
     * @param uuid UUID of the player.
     * @return player.html with all placeholders replaced.
     */
    public String getPlayerHtmlAsString(UUID uuid) {
        WebServer server = plugin.getUiServer();
        if (Verify.notNull(server)) {
            return server.getDataReqHandler().getInspectHtml(uuid);
        }
        DataRequestHandler reqH = new DataRequestHandler(plugin);
        return reqH.getInspectHtml(uuid);
    }

    /**
     * Check if the Analysis has been run and is cached to the AnalysisCache.
     *
     * @return true/false
     */
    public boolean isAnalysisCached() {
        // TODO Check PageCache
        return false;
    }

    /**
     * Run's the analysis with the current data in the cache and fetches rest
     * from the database.
     * <p>
     * Starts a new Asynchronous task to run the analysis.
     */
    public void updateAnalysisCache() {
        // TODO Run analysis
    }

    /**
     * Used to get the full HTML of the Analysis page as a string.
     * <p>
     * Check if the data is cached to AnalysisCache before calling this.
     *
     * @return server.html with all placeholders replaced.
     */
    public String getAnalysisHtmlAsString() {
        WebServer server = plugin.getUiServer();
        if (Verify.notNull(server)) {
            return server.getDataReqHandler().getServerHtml();
        }
        DataRequestHandler reqH = new DataRequestHandler(plugin);
        return reqH.getServerHtml();
    }

    /**
     * Used to get the AnalysisData object.
     * <p>
     * Check if the data is cached to AnalysisCache before calling this.
     *
     * @return AnalysisData object.
     * @see AnalysisData
     */
    public AnalysisData getAnalysisDataFromCache() {
        // TODO Fix
        return null;
    }

    /**
     * Used to get the PlayerName of a player who has played on the server.
     *
     * @param uuid UUID of the player.
     * @return PlayerName, eg "Rsl1122"
     * @throws IllegalArgumentException If uuid is null.
     * @throws IllegalStateException    If the player has not played on the server
     *                                  before.
     */
    public String getPlayerName(UUID uuid) {
        Verify.nullCheck(uuid);
        IOfflinePlayer offlinePlayer = Fetch.getIOfflinePlayer(uuid);
        if (Verify.notNull(offlinePlayer)) {
            return offlinePlayer.getName();
        }
        throw new IllegalStateException("Player has not played on this server before.");
    }

    /**
     * Uses UUIDUtility to turn PlayerName to UUID.
     *
     * @param playerName Player's name
     * @return UUID of the Player
     * @throws Exception if player's name is not registered at Mojang
     * @deprecated Typo in method name, use playerNameToUUID instead
     */
    @Deprecated
    public UUID PlayerNameToUUID(String playerName) throws Exception {
        return playerNameToUUID(playerName);
    }

    /**
     * Uses UUIDUtility to turn PlayerName to UUID.
     *
     * @param playerName Player's name
     * @return UUID of the Player
     * @throws IllegalArgumentException if player's name is not registered at Mojang
     */
    public UUID playerNameToUUID(String playerName) {
        UUID uuid = UUIDUtility.getUUIDOf(playerName);
        if (uuid == null) {
            throw new IllegalArgumentException("UUID did not get a match");
        }
        return uuid;
    }

    /**
     * Get the saved UUIDs in the database.
     * <p>
     * Should be called from async thread.
     *
     * @return Collection of UUIDs that can be found in the database.
     * @throws SQLException If database error occurs.
     * @since 3.4.2
     */
    public Collection<UUID> getSavedUUIDs() throws SQLException {
        return plugin.getDB().getSavedUUIDs();
    }
}
