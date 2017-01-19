package com.djrapitops.plan.api;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanLiteHook;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.ui.DataRequestHandler;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.planlite.UUIDFetcher;
import java.util.Date;
import java.util.UUID;
import main.java.com.djrapitops.plan.ui.webserver.WebSocketServer;

/**
 *
 * @author Rsl1122
 */
public class API {

    private Plan plugin;
    private PlanLiteHook hook;

    /**
     * Class Construcor.
     *
     * @param plugin Current instance of Plan
     */
    public API(Plan plugin) {
        this.plugin = plugin;
        hook = plugin.getPlanLiteHook();
    }

    /**
     * Returns a user readable format of Time difference between two dates
     *
     * @param before Date with long value that is lower
     * @param after Date with long value that is higher
     * @return String that is easily readable d:h:m:s
     */
    public static String formatTimeSinceDate(Date before, Date after) {
        return FormatUtils.formatTimeAmountSinceDate(before, after);
    }

    /**
     * Returns a user readable format of Time difference between two dates
     *
     * @param before String of long since Epoch 1970
     * @param after Date with long value that is higher
     * @return String that is easily readable d:h:m:s
     */
    public static String formatTimeSinceString(String before, Date after) {
        return FormatUtils.formatTimeAmountSinceString(before, after);
    }

    /**
     * Returns a user readable format of Time
     *
     * @param timeInMs String of long value in milliseconds
     * @return String that is easily readable d:h:m:s
     */
    public static String formatTimeAmount(String timeInMs) {
        return FormatUtils.formatTimeAmount(timeInMs);
    }

    /**
     * Returns user readable format of a Date.
     *
     * @param timeInMs String of long since Epoch 1970
     * @return String that is easily readable date.
     */
    public static String formatTimeStamp(String timeInMs) {
        return FormatUtils.formatTimeStamp(timeInMs);
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

    /**
     * Caches the UserData to the InspectCache for time specified in the Plan
     * config, so it can be called by webserver.
     *
     * Does not cache anything if the player has not joined the server or has no
     * data in the database.
     *
     * @param uuid UUID of the Player
     */
    public void cacheUserDataToInspectCache(UUID uuid) {
        plugin.getInspectCache().cache(uuid);
    }

    /**
     * Returns the ip:port/player/playername html as a string so it can be
     * integrated into other webserver plugins.
     *
     * Should use cacheUserDataToInspectCache(UUID uuid) before using this method.
     * 
     * If UserData of the specified player is not in the Cache returns <h1>404
     * Data was not found in cache</h1>
     *
     * @param uuid UUID of the Player
     * @return html as a string or a single error line html.
     */
    public String getPlayerHtmlAsString(UUID uuid) {
        WebSocketServer server = plugin.getUiServer();
        if (server != null) {
            return server.getDataReqHandler().getDataHtml(uuid);
        }
        DataRequestHandler reqH = new DataRequestHandler(plugin);
        return reqH.getDataHtml(uuid);
    }

    /**
     * Updates the AnalysisCache so the cached data can be called by the
     * webserver.
     */
    public void updateAnalysisCache() {
        plugin.getAnalysisCache().updateCache();
    }

    /**
     * Returns the ip:port/server html as a string so it can be integrated into
     * other webserver plugins.
     *
     * Should use updateAnalysisCache() before using this method.
     * 
     * If AnalysisData is not in the AnalysisCache: returns <h1>404 Data was not
     * found in cache</h1>
     *
     * @return html as a string or a single error line html.
     */
    public String getAnalysisHtmlAsString() {
        WebSocketServer server = plugin.getUiServer();
        if (server != null) {
            return server.getDataReqHandler().getAnalysisHtml();
        }
        DataRequestHandler reqH = new DataRequestHandler(plugin);
        return reqH.getAnalysisHtml();
    }

    /**
     * Returns UserData from the InspectCache
     *
     * @param uuid UUID of the Player
     * @return UserData of the Player in the InspectCache or null if not found
     */
    public UserData getUserDataFromInspectCache(UUID uuid) {
        return plugin.getInspectCache().getFromCache(uuid);
    }

    /**
     * Returns AnalysisData from the AnalysisCache
     *
     * @return AnalysisData in the AnalysisCache or null if not found
     */
    public AnalysisData getAnalysisDataFromCache() {
        return plugin.getAnalysisCache().getData();
    }
}
