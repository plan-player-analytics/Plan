package main.java.com.djrapitops.plan.data.handling;

import main.java.com.djrapitops.plan.data.UserData;

/**
 * Class containing static methods for processing information contained in a
 * QuitEvent.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class LogoutHandling {

    /**
     * Processes the information of the Event and changes UserData object
     * accordingly.
     *
     * @param data   UserData of the player.
     * @param time   Epoch ms the event occurred.
     * @param banned Is the player banned?
     */
    public static void processLogoutInfo(UserData data, long time, boolean banned) {
        data.setPlayTime(data.getPlayTime() + (time - data.getLastPlayed()));
        data.setLastPlayed(time);
        data.updateBanned(banned);
    }
}
