package main.java.com.djrapitops.plan.data.handling;

import com.djrapitops.plugin.utilities.player.Gamemode;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.time.GMTimes;

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

        long diff = time - data.getLastPlayed();
        long playTime = data.getPlayTime() + diff;
        data.setPlayTime(playTime);
        data.setLastPlayed(time);

        GMTimes gmTimes = data.getGmTimes();
        gmTimes.changeState(newGamemode, playTime);
    }
}
