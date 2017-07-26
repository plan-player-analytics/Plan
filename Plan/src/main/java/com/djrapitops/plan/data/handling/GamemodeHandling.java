package main.java.com.djrapitops.plan.data.handling;

import com.djrapitops.plugin.utilities.player.Gamemode;
import main.java.com.djrapitops.plan.data.UserData;

import java.util.Map;

/**
 * Class containing static methods for processing information contained in a
 * GamemodeChangeEvent.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class GamemodeHandling {

    /**
     * Processes the information of the Event and changes UserData object
     * accordingly.
     *
     * @param data  UserData of the player.
     * @param time  Epoch ms the event occurred.
     * @param newGM The Gamemode the player changed to.
     */
    public static void processGamemodeInfo(UserData data, long time, Gamemode newGM) {
        if (newGM == null) {
            return;
        }
        final String newGamemode = newGM.name();
        String lastGamemode = data.getLastGamemode();

        if (lastGamemode == null) {
            data.setLastGamemode(newGamemode);
        }
        lastGamemode = data.getLastGamemode();
        Map<String, Long> times = data.getGmTimes();
        long currentGMTime = times.getOrDefault(lastGamemode, 0L);
        data.setPlayTime(data.getPlayTime() + (time - data.getLastPlayed()));
        data.setLastPlayed(time);
        long lastSwap = data.getLastGmSwapTime();
        long playtime = data.getPlayTime();
        data.setGMTime(lastGamemode, currentGMTime + (playtime - lastSwap));
        data.setLastGmSwapTime(playtime);
        data.setLastGamemode(newGamemode);
    }
}
