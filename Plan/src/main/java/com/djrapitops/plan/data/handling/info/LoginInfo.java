package main.java.com.djrapitops.plan.data.handling.info;

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
    private final PlaytimeDependentInfo playtimeDependentInfo;
    private final int loginTimes;

    /**
     * Constructor.
     *
     * @param uuid       UUID of the player.
     * @param time       Epoch ms of the event.
     * @param ip         IP of the player
     * @param banned     Is the player banned?
     * @param nickname   Nickname of the player
     * @param gm         current gamemode of the player
     * @param loginTimes number the loginTimes should be incremented with.
     */
    public LoginInfo(UUID uuid, long time, InetAddress ip, boolean banned, String nickname, String gm, int loginTimes, String worldName) {
        super(uuid, InfoType.LOGIN, time);
        this.ip = ip;
        this.banned = banned;
        this.nickname = nickname;
        this.playtimeDependentInfo = new PlaytimeDependentInfo(uuid, InfoType.OTHER, time, gm, worldName);
        this.loginTimes = loginTimes;
    }

    /**
     * Constructor for not incrementing the loginTimes.
     * <p>
     * This constructor is used only by ReloadInfo
     *
     * @param uuid     UUID of the player.
     * @param time     Epoch ms of the event.
     * @param ip       IP of the player
     * @param banned   Is the player banned?
     * @param nickname Nickname of the player
     * @param gm       current gamemode of the player
     */
    public LoginInfo(UUID uuid, long time, InetAddress ip, boolean banned, String nickname, String gm, String worldName) {
        super(uuid, InfoType.RELOAD, time);
        this.ip = ip;
        this.banned = banned;
        this.nickname = nickname;
        this.playtimeDependentInfo = new PlaytimeDependentInfo(uuid, InfoType.OTHER, time, gm, worldName);
        this.loginTimes = 0;
    }

    @Override
    public void process(UserData uData) {
        if (!uData.getUuid().equals(uuid)) {
            return;
        }
        LoginHandling.processLoginInfo(uData, time, ip, banned, nickname, loginTimes);
        playtimeDependentInfo.process(uData);
    }
}
