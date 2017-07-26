package main.java.com.djrapitops.plan.data.handling.info;

import com.djrapitops.plugin.utilities.player.Gamemode;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.LogoutHandling;

import java.util.UUID;

/**
 * HandlingInfo Class for QuitEvent information.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class LogoutInfo extends HandlingInfo {

    private final boolean banned;
    private final SessionData sData;
    private final GamemodeInfo gmInfo;

    /**
     * Constructor.
     *
     * @param uuid UUID of the player.
     * @param time Epoch ms of the event.
     * @param banned Is the player banned
     * @param gm current gamemode of the player
     * @param sData session that has been ended at the moment of the logout
     * event.
     */
    public LogoutInfo(UUID uuid, long time, boolean banned, Gamemode gm, SessionData sData) {
        super(uuid, InfoType.LOGOUT, time);
        this.banned = banned;
        this.sData = sData;
        this.gmInfo = new GamemodeInfo(uuid, time, gm);
    }

    @Override
    public boolean process(UserData uData) {
        if (!uData.getUuid().equals(uuid)) {
            return false;
        }
        uData.addSession(sData);
        LogoutHandling.processLogoutInfo(uData, time, banned);
        gmInfo.process(uData);
        return true;
    }

}
