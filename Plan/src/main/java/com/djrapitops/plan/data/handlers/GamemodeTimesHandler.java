package main.java.com.djrapitops.plan.data.handlers;

import java.util.HashMap;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import org.bukkit.GameMode;

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
        handleChangeEvent(gm, data);
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
        if (newGM == null) {
            return;
        }
        if (data.getLastGamemode() == null) {
            data.setLastGamemode(GameMode.SURVIVAL);
        }
        GameMode oldGM = data.getLastGamemode();
        HashMap<GameMode, Long> times = data.getGmTimes();
        Long currentGMTime = times.get(oldGM);
        if (currentGMTime == null) {
            currentGMTime = 0L;
        }
        handler.getActivityHandler().saveToCache(data);
        long lastSwap = data.getLastGmSwapTime();
        long playTime = data.getPlayTime();
        data.setGMTime(oldGM, currentGMTime + (playTime - lastSwap));
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
        if (currentGM == null) {
            return;
        }
        HashMap<GameMode, Long> times = data.getGmTimes();
        handler.getActivityHandler().saveToCache(data);
        Long currentGMTime = times.get(currentGM);
        if (currentGMTime == null) {
            currentGMTime = 0L;
        }
        long lastSwap = data.getLastGmSwapTime();
        long playtime = data.getPlayTime();
        data.setGMTime(currentGM, currentGMTime + (playtime - lastSwap));
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
