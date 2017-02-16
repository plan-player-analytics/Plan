package main.java.com.djrapitops.plan.data.handlers;

import java.util.HashMap;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
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
     * @param gm Gamemode upon login
     * @param data UserData matching the Player
     */
    public void handleLogin(GameMode gm, UserData data) {
        data.setLastGamemode(gm);
    }

    /**
     * Updates the GameModeTimes HashMap.
     *
     * Updates GMTimes with new values and sets lastSwap and lastGM.
     *
     * @param newGM the GameMode player changed to
     * @param data UserData matching the Player
     */
    public void handleChangeEvent(GameMode newGM, UserData data) {
        HashMap<GameMode, Long> times = data.getGmTimes();
        handler.getActivityHandler().saveToCache(data);

        long lastSwap = data.getLastGmSwapTime();
        long playTime = data.getPlayTime();
        GameMode oldGM = data.getLastGamemode();
        data.setGMTime(oldGM, times.get(oldGM) + (playTime - lastSwap));

        data.setLastGamemode(newGM);

        data.setLastGmSwapTime(playTime);
    }

    /**
     * Updates GMTimes with new values and saves it to cache.
     *
     * @param currentGM Current Gamemode of the Player
     * @param data UserData matching the Player
     */
    public void saveToCache(GameMode currentGM, UserData data) {
        HashMap<GameMode, Long> times = data.getGmTimes();
        handler.getActivityHandler().saveToCache(data);

        long lastSwap = data.getLastGmSwapTime();
        long playtime = data.getPlayTime();
        data.setGMTime(currentGM, times.get(currentGM) + (playtime - lastSwap));

        data.setLastGmSwapTime(playtime);
    }

    /**
     * Updates GMTImes for player who is online when /reload is run.
     *
     * @param currentGM Gamemode if online during reload
     * @param data UserData matching Player
     */
    public void handleReload(GameMode currentGM, UserData data) {
        saveToCache(currentGM, data);
    }

    /**
     * Updates GMTimes on Logout.
     *
     * @param currentGM Current gamemode at logout
     * @param data UserData matching Player
     */
    public void handleLogOut(GameMode currentGM, UserData data) {
        saveToCache(currentGM, data);
    }
}
