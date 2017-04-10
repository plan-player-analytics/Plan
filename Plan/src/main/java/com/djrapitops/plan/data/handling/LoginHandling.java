/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;

/**
 *
 * @author Risto
 */
public class LoginHandling {

    public static void processLoginInfo(UserData data, long time, InetAddress ip, boolean banned, String nickname, int loginTimes) {
        data.setLastPlayed(time);
        data.updateBanned(banned);
        data.setLoginTimes(data.getLoginTimes() + loginTimes);
        data.addNickname(nickname);
        data.addIpAddress(ip);
        updateGeolocation(ip, data);
    }

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
