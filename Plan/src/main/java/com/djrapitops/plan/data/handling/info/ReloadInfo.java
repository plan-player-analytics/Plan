package main.java.com.djrapitops.plan.data.handling.info;

import com.djrapitops.plugin.utilities.player.Gamemode;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.LoginHandling;

import java.net.InetAddress;
import java.util.UUID;

/**
 * HandlingInfo Class for refreshing data in the cache for online players.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class ReloadInfo extends HandlingInfo {

    private final InetAddress ip;
    private final boolean banned;
    private final String nickname;
    private final GamemodeInfo gmInfo;

    /**
     * Constructor.
     *
     * @param uuid UUID of the player.
     * @param time Epoch ms of the event.
     * @param ip IP of the player
     * @param banned Is the player banned?
     * @param nickname Nickname of the player
     * @param gm current gamemode of the player
     */
    public ReloadInfo(UUID uuid, long time, InetAddress ip, boolean banned, String nickname, Gamemode gm) {
        super(uuid, InfoType.RELOAD, time);
        this.ip = ip;
        this.banned = banned;
        this.nickname = nickname;
        gmInfo = new GamemodeInfo(uuid, time, gm);
    }

    @Override
    public boolean process(UserData uData) {
        if (!uData.getUuid().equals(uuid)) {
            return false;
        }
        uData.setPlayTime(uData.getPlayTime() + (time - uData.getLastPlayed()));
        uData.setLastPlayed(time);
        uData.updateBanned(banned);
        uData.addIpAddress(ip);
        uData.addNickname(nickname);
        LoginHandling.updateGeolocation(ip, uData);
        return gmInfo.process(uData);
    }

}
