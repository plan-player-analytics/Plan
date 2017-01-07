package com.djrapitops.plan.data.handlers;

import com.djrapitops.plan.data.cache.DataCacheHandler;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.UserData;
import java.util.Date;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
        return !handler.getDB().wasSeenBefore(uuid);
    }

    /**
     * Saves current PlayTime timer and sets lastPlayed.
     *
     * lastPlayed is set to long matching current Date.
     *
     * @param player Player which data is being saved
     * @param data UserData matching the Player
     */
    public void saveToCache(Player player, UserData data) {
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
     * @param event JoinEvent from listener
     * @param data UserData matching the Player
     */
    public void handleLogin(PlayerJoinEvent event, UserData data) {
        data.setLastPlayed(new Date().getTime());
        Player player = event.getPlayer();
        data.updateBanned(player);
        data.setLoginTimes(data.getLoginTimes() + 1);
        handler.getLocationHandler().addLocation(player.getUniqueId(), player.getLocation());
    }

    /**
     * Updates UserData about activity related things on Logout.
     *
     * Saves PlayTime, Set's LastPlayed
     * value to long matching current Date
     *
     * @param event QuitEvent from Listener
     * @param data UserData matching the Player
     */
    public void handleLogOut(PlayerQuitEvent event, UserData data) {
        long timeNow = new Date().getTime();
        data.setPlayTime(data.getPlayTime() + (timeNow - data.getLastPlayed()));
        data.setLastPlayed(timeNow);
    }

    /**
     * Updates UserData about activity related things on /reload.
     *
     * Updates PlayTime, Sets LastPlayed value to long matching current Date
     *
     * @param player Player who is online
     * @param data UserData matching the Player
     */
    public void handleReload(Player player, UserData data) {
        long timeNow = new Date().getTime();
        data.setPlayTime(data.getPlayTime() + (timeNow - data.getLastPlayed()));
        data.setLastPlayed(timeNow);
    }
}
