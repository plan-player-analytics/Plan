package main.java.com.djrapitops.plan.data.handling;

import main.java.com.djrapitops.plan.data.UserData;

/**
 * Class containing static methods for processing information contained in a
 * ChatEvent.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class ChatHandling {

    /**
     * Processes the information of the Event and changes UserData object
     * accordingly.
     *
     * @param data     UserData of the player.
     * @param nickname Nickname of the player during the event.
     * @param msg      Message sent by the player.
     */
    public static void processChatInfo(UserData data, String nickname, String msg) {
        data.addNickname(nickname);
    }
}
