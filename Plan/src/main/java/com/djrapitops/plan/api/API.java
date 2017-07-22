package main.java.com.djrapitops.plan.api;

import com.djrapitops.plugin.utilities.Verify;
import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import com.djrapitops.plugin.utilities.player.UUIDFetcher;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.ui.html.DataRequestHandler;
import main.java.com.djrapitops.plan.ui.webserver.WebSocketServer;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 * This class contains the API methods.
 *
 * Methods can be called from Asyncronous task and are thread safe unless
 * otherwise stated.
 *
 * Use Plan.getPlanAPI() to get the API.
 *
 * More information about API methods can be found on Github.
 *
 * @author Rsl1122
 * @since 2.0.0
 * @see PluginData
 * @see AnalysisType
 * @see DBCallableProcessor
 * @see HandlingInfo
 */
public class API {

    private final Plan plugin;

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
     * Analysis and Inspect to manage the data of a plugin correctly.
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
        return HtmlUtils.getInspectUrlWithProtocol(name);
    }

    /**
     * Schedule a UserData object to be fetched from the database or cache if
     * the player is online.
     *
     * The data will not be cached if it is not already cached.
     *
     * @param uuid UUID of the player.
     * @param processor Object implementing DBCallableProcessor, which
     * process(UserData data) method will be called.
     */
    public void scheduleForGet(UUID uuid, DBCallableProcessor processor) {
        plugin.getHandler().getUserDataForProcessing(processor, uuid, false);
    }

    /**
     * Schedule a HandlingInfo object to be processed.
     *
     * UserData associated with the UUID of the HandlingInfo object will be
     * cached.
     *
     * @param info object that extends HandlingInfo.
     */
    public void scheduleEventHandlingInfo(HandlingInfo info) {
        plugin.getHandler().addToPool(info);
    }

    /**
     * Used to cache a UserData object.
     *
     * If data is already cached it will be overridden.
     *
     * @param data UserData object. Will be placed to the data.getUuid() key in
     * the cache.
     */
    public void placeDataToCache(UserData data) {
        plugin.getHandler().cache(data);
    }

    /**
     * Used to save the cached data to the database.
     *
     * Should be only called from an Asyncronous thread.
     */
    public void saveCachedData() {
        plugin.getHandler().saveCachedUserData();
    }

    /**
     * Check if the UserData is cached to the InspectCache.
     *
     * @param uuid UUID of the player.
     * @return true/false
     */
    public boolean isPlayersDataInspectCached(UUID uuid) {
        return plugin.getInspectCache().isCached(uuid);
    }

    /**
     * Cache the UserData to InspectCache.
     *
     * Uses cache if data is cached or database if not. Call from an Asyncronous
     * thread.
     *
     * @param uuid UUID of the player.
     */
    public void cacheUserDataToInspectCache(UUID uuid) {
        plugin.getInspectCache().cache(uuid);
    }

    /**
     * Used to get the full Html of the Inspect page as a string.
     *
     * Check if the data is cached to InspectCache before calling this.
     *
     * @param uuid UUID of the player.
     * @return player.html with all placeholders replaced.
     */
    public String getPlayerHtmlAsString(UUID uuid) {
        WebSocketServer server = plugin.getUiServer();
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
        return plugin.getAnalysisCache().isCached();
    }

    /**
     * Run's the analysis with the current data in the cache and fetches rest
     * from the database.
     *
     * Starts a new Asyncronous task to run the analysis.
     */
    public void updateAnalysisCache() {
        plugin.getAnalysisCache().updateCache();
    }

    /**
     * Used to get the full Html of the Analysis page as a string.
     *
     * Check if the data is cached to AnalysisCache before calling this.
     *
     * @return analysis.html with all placeholders replaced.
     */
    public String getAnalysisHtmlAsString() {
        WebSocketServer server = plugin.getUiServer();
        if (Verify.notNull(server)) {
            return server.getDataReqHandler().getAnalysisHtml();
        }
        DataRequestHandler reqH = new DataRequestHandler(plugin);
        return reqH.getAnalysisHtml();
    }

    /**
     * Used to get the AnalysisData object.
     *
     * Check if the data is cached to AnalysisCache before calling this.
     *
     * @return AnalysisData object.
     * @see AnalysisData
     */
    public AnalysisData getAnalysisDataFromCache() {
        return plugin.getAnalysisCache().getData();
    }

    /**
     * Used to get the playerName of a player who has played on the server.
     *
     * @param uuid UUID of the player.
     * @return Playername, eg "Rsl1122"
     * @throws IllegalArgumentException If uuid is null.
     * @throws IllegalStateException If the player has not played on the server
     * before.
     */
    public String getPlayerName(UUID uuid) throws IllegalStateException, IllegalArgumentException {
        Verify.nullCheck(uuid);
        IOfflinePlayer offlinePlayer = Fetch.getIOfflinePlayer(uuid);
        if (Verify.notNull(offlinePlayer)) {
            return offlinePlayer.getName();
        }
        throw new IllegalStateException("Player has not played on this server before.");
    }

    /**
     * Uses UUIDFetcher to turn PlayerName to UUID.
     *
     * @param playerName Player's name
     * @return UUID of the Player
     * @throws Exception if player's name is not registered at Mojang
     */
    public UUID playerNameToUUID(String playerName) throws Exception {
        return UUIDFetcher.getUUIDOf(playerName);
    }

    /**
     * Get the saved UUIDs in the database.
     *
     * Should be called from async thread.
     *
     * @return Collection of UUIDs that can be found in the database.
     * @throws SQLException If database error occurs.
     * @since 3.4.2
     */
    public Collection<UUID> getSavedUUIDs() throws SQLException {
        return plugin.getDB().getSavedUUIDs();
    }

    /**
     * Get the saved UserData in the database for a collection of UUIDs.
     *
     * Will not contain data for UUIDs not found in the database.
     *
     * Should be called from async thread.
     *
     * @param uuids Collection of UUIDs that can be found in the database.
     * @return List of all Data in the database.
     * @throws SQLException If database error occurs.
     * @since 3.4.2
     */
    public List<UserData> getUserDataOfUsers(Collection<UUID> uuids) throws SQLException {
        return plugin.getDB().getUserDataForUUIDS(uuids);
    }

    /**
     * Get the cached UserData objects in the InspectCache.
     *
     * This can be used with PluginData objects safely to get the data for all
     * users in Plan database, because all data is InspectCached before analysis
     * begins.
     *
     * @return List of all Data in the InspectCache.
     * @since 3.5.0
     */
    public List<UserData> getInspectCachedUserData() {
        return plugin.getInspectCache().getCachedUserData();
    }

    /**
     * Get the cached UserData objects in the InspectCache in a Map form.
     *
     * This can be used with PluginData objects safely to get the data for all
     * users in Plan database, because all data is InspectCached before analysis
     * begins.
     *
     * @return Map of all Data in the InspectCache with UUID of the player as
     * the key.
     * @since 3.5.0
     */
    public Map<UUID, UserData> getInspectCachedUserDataMap() {
        return getInspectCachedUserData().stream().collect(Collectors.toMap(UserData::getUuid, Function.identity()));
    }
}
