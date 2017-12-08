package main.java.com.djrapitops.plan.api;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.ParseException;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.plugin.PluginData;
import main.java.com.djrapitops.plan.systems.info.BukkitInformationManager;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static org.bukkit.Bukkit.getOfflinePlayer;

/**
 * This class contains the API methods for Bukkit version of the plugin.
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
 * @since 4.0.0
 */
public class API {

    private final Plan plugin;

    /**
     * Creates a new API instance - not supposed to be called outside {@code Plan.onEnable}.
     *
     * @param plugin Current instance of Plan
     */
    public API(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     * Condition whether or not the plugin enabled successfully.
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
     * Used to get a relative link to InspectPage of a player.
     * <p>
     * This method is useful if you have a table and want to link to the inspect
     * page.
     * <p>
     * Html.LINK.parse("Link", "PlayerName") can be used to get a link
     * {@code <a href="Link">PlayerName</a>}
     *
     * @param name Name of the player
     * @return {@code ../player/PlayerName}
     */
    public String getPlayerInspectPageLink(String name) {
        if (name == null) {
            return  "#";
        }
        return "../player/" + name.replace(" ", "%20").replace(".", "%2E");
    }

    /**
     * Condition if Players's Inspect page is cached to PageCache.
     *
     * @param uuid UUID of the player.
     * @return true/false
     * @deprecated use {@code isPlayerHtmlCached}
     */
    @Deprecated
    public boolean isPlayersDataInspectCached(UUID uuid) {
        return isPlayerHtmlCached(uuid);
    }

    /**
     * Condition if Players's Inspect page is cached to PageCache of the providing WebServer.
     * <p>
     * Using BungeeCord: Will send a {@code IsCachedWebAPI} request to check if the page is in Bungee's PageCache.
     * Only Bukkit: Checks PageCache for page.
     *
     * @param uuid UUID of the player.
     * @return true/false
     */
    public boolean isPlayerHtmlCached(UUID uuid) {
        return plugin.getInfoManager().isCached(uuid);
    }

    /**
     * Cache Players's Inspect page to the PageCache of the providing WebServer.
     *
     * @param uuid UUID of the player.
     * @deprecated use {@code cachePlayerHtml}
     */
    @Deprecated
    public void cacheUserDataToInspectCache(UUID uuid) {
        cachePlayerHtml(uuid);
    }

    /**
     * Cache Players's Inspect page to the PageCache of the providing WebServer.
     * <p>
     * Using BungeeCord: Will send a {@code PostHtmlWebAPI} request after calculating the inspect page.
     * Only Bukkit: Calculates inspect page and places it in the PageCache.
     *
     * @param uuid UUID of the player.
     * @deprecated use {@code cachePlayerHtml}
     */
    public void cachePlayerHtml(UUID uuid) {
        plugin.getInfoManager().cachePlayer(uuid);
    }

    /**
     * Used to get the full Html of the Inspect page as a string.
     * <p>
     * Re-calculates the inspect html on this server.
     *
     * @param uuid UUID of the player.
     * @return player.html with all placeholders replaced.
     */
    public String getPlayerHtmlAsString(UUID uuid) throws ParseException {
        return plugin.getInfoManager().getPlayerHtml(uuid);
    }

    /**
     * Condition if the Analysis has been run and is cached to the AnalysisCache.
     *
     * @return true/false
     */
    public boolean isAnalysisCached() {
        return plugin.getInfoManager().isAnalysisCached(Plan.getServerUUID());
    }

    /**
     * Run the analysis.
     */
    public void updateAnalysisCache() {
        plugin.getInfoManager().refreshAnalysis(plugin.getServerUuid());
    }

    /**
     * Used to get the full HTML of the Analysis page as a string.
     * <p>
     * Condition if the data is cached to AnalysisCache before calling this.
     *
     * @return server.html with all placeholders replaced.
     * @throws NullPointerException if AnalysisData has not been cached.
     */
    public String getAnalysisHtmlAsString() {
        return plugin.getInfoManager().getAnalysisHtml();
    }

    /**
     * Used to get the AnalysisData object.
     * <p>
     * Condition if the data is cached to AnalysisCache before calling this.
     *
     * @return AnalysisData object.
     * @see AnalysisData
     */
    public AnalysisData getAnalysisDataFromCache() {
        return ((BukkitInformationManager) plugin.getInfoManager()).getAnalysisData();
    }

    /**
     * Used to get the PlayerName of a player who has played on the server.
     * Should be called from an Async thread.
     *
     * @param uuid UUID of the player.
     * @return PlayerName, eg "Rsl1122"
     * @throws IllegalArgumentException If uuid is null.
     * @throws IllegalStateException    If the player has not played on the server before.
     */
    public String getPlayerName(UUID uuid) throws SQLException {
        Verify.nullCheck(uuid);
        String playerName = plugin.getDB().getUsersTable().getPlayerName(uuid);
        if (playerName != null) {
            return playerName;
        }
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        if (offlinePlayer != null) {
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
