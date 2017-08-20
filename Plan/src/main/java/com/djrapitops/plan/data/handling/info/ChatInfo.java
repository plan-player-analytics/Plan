package main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.ChatHandling;

import java.util.UUID;

/**
 * HandlingInfo Class for ChatEvent information.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class ChatInfo extends HandlingInfo {

    private final String nickname;

    /**
     * Constructor.
     *
     * @param uuid     UUID of the player.
     * @param nickname Nickname of the player.
     */
    public ChatInfo(UUID uuid, String nickname) {
        super(uuid, InfoType.CHAT, 0L);
        this.nickname = nickname;
    }

    @Override
    public void process(UserData uData) {
        if (!uData.getUuid().equals(uuid)) {
            return;
        }
        ChatHandling.processChatInfo(uData, nickname);
    }
}
