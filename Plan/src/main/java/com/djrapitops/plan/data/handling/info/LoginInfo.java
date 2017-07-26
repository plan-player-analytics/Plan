package main.java.com.djrapitops.plan.data.handling.info;

import com.djrapitops.plugin.utilities.player.Gamemode;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.LoginHandling;

import java.net.InetAddress;
import java.util.UUID;

/**
 * HandlingInfo Class for JoinEvent information.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class LoginInfo extends HandlingInfo {

    private final InetAddress ip;
    private final boolean banned;
    private final String nickname;
    private final GamemodeInfo gmInfo;
    private final int loginTimes;

    /**
     * Constructor.
     *
     * @param uuid UUID of the player.
     * @param time Epoch ms of the event.
     * @param ip IP of the player
     * @param banned Is the player banned?
     * @param nickname Nickname of the player
     * @param gm current gamemode of the player
     * @param loginTimes number the loginTimes should be incremented with.
     */
    public LoginInfo(UUID uuid, long time, InetAddress ip, boolean banned, String nickname, Gamemode gm, int loginTimes) {
        super(uuid, InfoType.LOGIN, time);
        this.ip = ip;
        this.banned = banned;
        this.nickname = nickname;
        this.gmInfo = new GamemodeInfo(uuid, time, gm);
        this.loginTimes = loginTimes;
    }

    /**
     * Constructor for not incrementing the loginTimes.
     *
     * @param uuid UUID of the player.
     * @param time Epoch ms of the event.
     * @param ip IP of the player
     * @param banned Is the player banned?
     * @param nickname Nickname of the player
     * @param gm current gamemode of the player
     */
    public LoginInfo(UUID uuid, long time, InetAddress ip, boolean banned, String nickname, Gamemode gm) {
        this(uuid, time, ip, banned, nickname, gm, 0);
    }

    @Override
    public boolean process(UserData uData) {
        if (!uData.getUuid().equals(uuid)) {
            return false;
        }
        LoginHandling.processLoginInfo(uData, time, ip, banned, nickname, loginTimes);
        gmInfo.process(uData);
        return true;
    }
}
