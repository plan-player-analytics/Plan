package main.java.com.djrapitops.plan.data.handlers;

import java.net.InetAddress;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;

/**
 *
 * @author Rsl1122
 */
public class BasicInfoHandler {

    private DataCacheHandler handler;

    /**
     * Class Constructor
     *
     * @param plugin Current instance of Plan
     * @param h Current instance of DataCacheHandler
     */
    public BasicInfoHandler(Plan plugin, DataCacheHandler h) {
        this.handler = h;
    }

    /**
     * Adds new nicknames and IPs to UserData
     *
     * @param nickname Displayname of player
     * @param ip IP of player
     * @param data UserData matching the Player
     */
    public void handleLogin(String nickname, InetAddress ip, UserData data) {
        addNickname(nickname, data);
        data.addIpAddress(ip);
    }

    /**
     * Adds new nicknames and IPs to UserData in case of /reload
     *
     * @param nickname Displayname of player
     * @param ip IP of player
     * @param data UserData matching the Player
     */
    public void handleReload(String nickname, InetAddress ip, UserData data) {
        addNickname(nickname, data);
        data.addIpAddress(ip);
    }

    /**
     *
     * @param nickname
     * @param data
     */
    public void addNickname(String nickname, UserData data) {
        if (!nickname.isEmpty()) {
            if (data.addNickname(nickname)) {
                data.setLastNick(nickname);
            }
        }
    }
}
