package main.java.com.djrapitops.plan.data.handling.info;

import java.net.InetAddress;
import java.util.UUID;

/**
 * HandlingInfo Class for refreshing data in the cache for online players.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class ReloadInfo extends LoginInfo {

    /**
     * Constructor.
     *
     * @param uuid     UUID of the player.
     * @param time     Epoch ms of the event.
     * @param ip       IP of the player
     * @param banned   Is the player banned?
     * @param nickname Nickname of the player
     * @param gm       current gamemode of the player
     */
    public ReloadInfo(UUID uuid, long time, InetAddress ip, boolean banned, String nickname, String gm, String worldName) {
        super(uuid, time, ip, banned, nickname, gm, worldName);
    }
}
