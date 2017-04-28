package main.java.com.djrapitops.plan.data.handling.info;

import java.util.UUID;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.ChatHandling;

/**
 *
 * @author Rsl1122
 */
public class ChatInfo extends HandlingInfo {
    private String nickname;
    private String message;

    /**
     *
     * @param uuid
     * @param nickname
     * @param message
     */
    public ChatInfo(UUID uuid, String nickname, String message) {
        super(uuid, InfoType.CHAT, 0L);
        this.nickname = nickname;
        this.message = message;
    }

    /**
     *
     * @return
     */
    public String getNickname() {
        return nickname;
    }

    /**
     *
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @param uData
     * @return
     */
    @Override
    public boolean process(UserData uData) {
        if (!uData.getUuid().equals(uuid)) {
            return false;
        }
        ChatHandling.processChatInfo(uData, nickname, message);
        return true;
    }
}
