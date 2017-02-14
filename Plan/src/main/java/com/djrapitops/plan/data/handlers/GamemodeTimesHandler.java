package main.java.com.djrapitops.plan.data.handlers;

import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import java.util.HashMap;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Rsl1122
 */
public class GamemodeTimesHandler {

    private final Plan plugin;
    private final DataCacheHandler handler;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     * @param h Current instance of DataCacheHandler
     */
    public GamemodeTimesHandler(Plan plugin, DataCacheHandler h) {
        this.plugin = plugin;
        handler = h;
    }

    /**
     * Updates lastGamemode to current gamemode on Login
     *
     * @param event JoinEvent from listener
     * @param data UserData matching the Player
     */
    public void handleLogin(PlayerJoinEvent event, UserData data) {
        data.setLastGamemode(event.getPlayer().getGameMode());
    }

    /**
     * Updates the GameModeTimes HashMap.
     *
     * Updates GMTimes with new values and sets lastSwap and lastGM.
     *
     * @param event GMChangeEvent from Listener
     * @param data UserData matching the Player
     */
    public void handleChangeEvent(PlayerGameModeChangeEvent event, UserData data) {
        HashMap<GameMode, Long> times = data.getGmTimes();
        handler.getActivityHandler().saveToCache(event.getPlayer(), data);

        long lastSwap = data.getLastGmSwapTime();
        long playTime = data.getPlayTime();
        GameMode oldGM = data.getLastGamemode();
        data.setGMTime(oldGM, times.get(oldGM) + (playTime - lastSwap));

        GameMode newGM = event.getNewGameMode();
        data.setLastGamemode(newGM);

        data.setLastGmSwapTime(playTime);
    }

    /**
     * Updates GMTimes with new values and saves it to cache.
     *
     * @param player Player whose data is being saved
     * @param data UserData matching the Player
     */
    public void saveToCache(Player player, UserData data) {
        HashMap<GameMode, Long> times = data.getGmTimes();
        handler.getActivityHandler().saveToCache(player, data);

        long lastSwap = data.getLastGmSwapTime();
        long playtime = data.getPlayTime();
        GameMode currentGM = player.getGameMode();
        data.setGMTime(currentGM, times.get(currentGM) + (playtime - lastSwap));

        data.setLastGmSwapTime(playtime);
    }

    /**
     * Updates GMTImes for player who is online when /reload is run.
     *
     * @param player Player whose data is updated
     * @param data UserData matching Player
     */
    public void handleReload(Player player, UserData data) {
        saveToCache(player, data);
    }

    /**
     * Updates GMTimes on Logout.
     *
     * @param event QuitEvent from Listener
     * @param data UserData matching Player
     */
    public void handleLogOut(PlayerQuitEvent event, UserData data) {
        saveToCache(event.getPlayer(), data);
    }
}
