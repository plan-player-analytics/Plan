package main.java.com.djrapitops.plan.data.handling;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.time.WorldTimes;

/**
 * Class for processing World Time related changes.
 *
 * @author Rsl1122
 * @since 3.6.0
 */
public class WorldTimeHandling {

    /**
     * Utility Class, hides constructor.
     */
    private WorldTimeHandling() {
        throw new IllegalStateException("Utility Class.");
    }

    /**
     * Processes the information of the Event and changes UserData object
     * accordingly.
     *
     * @param data      UserData of the player.
     * @param time      Epoch ms the event occurred.
     * @param worldName The World the player changed to.
     */
    public static void processWorldChangeInfo(UserData data, long time, String worldName) {
        if (worldName == null) {
            return;
        }

        long diff = time - data.getLastPlayed();
        long playTime = data.getPlayTime() + diff;
        data.setPlayTime(playTime);
        data.setLastPlayed(time);

        WorldTimes worldTimes = data.getWorldTimes();
        worldTimes.changeState(worldName, playTime);
    }
}
