package main.java.com.djrapitops.plan.data.handling;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.GeolocationCacheHandler;

import java.net.InetAddress;

/**
 * Class containing static methods for processing information contained in a
 * JoinEvent.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class LoginHandling {

    /**
     * Utility Class, hides constructor.
     */
    private LoginHandling() {
        throw new IllegalStateException("Utility Class.");
    }

    /**
     * Processes the information of the Event and changes UserData object
     * accordingly.
     *
     * @param data       UserData of the player.
     * @param time       Epoch ms the event occurred.
     * @param ip         IP of the player
     * @param banned     Is the player banned
     * @param nickname   Nickname of the player
     * @param loginTimes amount the loginTimes should be incremented with.
     */
    public static void processLoginInfo(UserData data, long time, InetAddress ip, boolean banned, String nickname, int loginTimes) {
        data.setLastPlayed(time);
        data.updateBanned(banned);
        data.setLoginTimes(data.getLoginTimes() + loginTimes);
        data.addNickname(nickname);
        data.addIpAddress(ip);
        updateGeolocation(ip, data);
    }

    /**
     * Updates the geolocation of the player.
     * <p>
     * Uses free service of freegeoip.net. 15000 requests can be sent per hour.
     *
     * @param ip   InetAddress used for location.
     * @param data UserData of the player.
     * @see GeolocationCacheHandler
     */
    public static void updateGeolocation(InetAddress ip, UserData data) {
        String geoLocation = GeolocationCacheHandler.getCountry(ip.getHostAddress());

        data.setGeolocation(geoLocation);
    }
}
