package main.java.com.djrapitops.plan.data.handlers;

import java.util.Date;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;

/**
 *
 * @author Rsl1122
 */
public class ActivityHandler {

    private final Plan plugin;
    private final DataCacheHandler handler;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     * @param h Current instance of DataCacheHandler
     */
    public ActivityHandler(Plan plugin, DataCacheHandler h) {
        this.plugin = plugin;
        this.handler = h;
    }

    /**
     * Checks from Database if the player's data is present.
     *
     * @param uuid Player's UUID
     * @return true if data is not found.
     */
    public boolean isFirstTimeJoin(UUID uuid) {
        return !plugin.getDB().wasSeenBefore(uuid);
    }

    /**
     * Saves current PlayTime timer and sets lastPlayed.
     *
     * lastPlayed is set to long matching current Date.
     *
     * @param data UserData matching the Player
     */
    public void saveToCache(UserData data) {
        long timeNow = new Date().getTime();
        data.setPlayTime(data.getPlayTime() + (timeNow - data.getLastPlayed()));
        data.setLastPlayed(timeNow);
    }

    /**
     * Updates UserData about activity related things on Login.
     *
     * Updates if player is banned or not, Adds one to login times, Adds current
     * location to location list.
     *
     * @param isBanned is the player banned?
     * @param data UserData matching the Player
     */
    public void handleLogin(boolean isBanned, UserData data) {
        data.setLastPlayed(new Date().getTime());
        data.updateBanned(isBanned);
        data.setLoginTimes(data.getLoginTimes() + 1);
        handler.getSessionHandler().startSession(data);
//        handler.getLocationHandler().addLocation(player.getUniqueId(), player.getLocation());
    }

    /**
     * Updates UserData about activity related things on Logout.
     *
     * Saves PlayTime, Set's LastPlayed value to long matching current Date
     *
     * @param data UserData matching the Player
     */
    public void handleLogOut(UserData data) {
        Date now = new Date();
        long timeNow = now.getTime();
        data.setPlayTime(data.getPlayTime() + (timeNow - data.getLastPlayed()));
        data.setLastPlayed(timeNow);
        handler.getSessionHandler().endSession(data);
    }

    /**
     * Updates UserData about activity related things on /reload.
     *
     * Updates PlayTime, Sets LastPlayed value to long matching current Date
     *
     * @param data UserData matching the Player
     */
    public void handleReload(UserData data) {
        Date now = new Date();
        long timeNow = now.getTime();
        data.setPlayTime(data.getPlayTime() + (timeNow - data.getLastPlayed()));
        data.setLastPlayed(timeNow);
        handler.getSessionHandler().startSession(data);
    }
}
