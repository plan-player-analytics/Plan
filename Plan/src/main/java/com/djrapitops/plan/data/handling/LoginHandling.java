package main.java.com.djrapitops.plan.data.handling;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;

/**
 * Class containing static methods for processing information contained in a
 * JoinEvent.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class LoginHandling {

    /**
     * Processes the information of the Event and changes UserData object
     * accordingly.
     *
     * @param data UserData of the player.
     * @param time Epoch ms the event occurred.
     * @param ip IP of the player
     * @param banned Is the player banned
     * @param nickname Nickname of the player
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
     *
     * Uses free service of freegeoip.net. 10000 requests can be sent per hour.
     *
     * @param ip InetAddress used for location.
     * @param data UserData of the player.
     */
    public static void updateGeolocation(InetAddress ip, UserData data) {
        DemographicsData demData = data.getDemData();
        try {
            String result = "";
            URL url = new URL("http://freegeoip.net/csv/" + ip.getHostAddress());
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String resultline;
            while ((resultline = in.readLine()) != null) {
                result += resultline + ",";
            }
            in.close();

            String[] results = result.split(",");
            if (!results[2].isEmpty()) {
                demData.setGeoLocation(results[2]);
            }
        } catch (Exception e) {
            demData.setGeoLocation(Phrase.DEM_UNKNOWN + "");
        }
    }
}
