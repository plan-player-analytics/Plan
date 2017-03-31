package main.java.com.djrapitops.plan.data.handlers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.Gender;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;

/**
 *
 * @author Rsl1122
 */
public class DemographicsHandler {

    private final DataCacheHandler handler;
    private final Plan plugin;

    /**
     * Class Constructor
     *
     * @param plugin Current instance of Plan.class
     * @param h Current instance of DataCacheHandler h
     */
    public DemographicsHandler(Plan plugin, DataCacheHandler h) {
        this.handler = h;
        this.plugin = plugin;
    }

    /**
     * Checks the message for Demographics relevant data
     *
     * If message contains triggerwords and words that define data important,
     * informatino will be saved in the DemographicsData of UserData provided
     *
     * @param message Chat Message
     * @param data UserData corresponding to player of this event.
     */
    public void handleChatEvent(String message, UserData data) {
        List<String> triggers = Arrays.asList(Settings.DEM_TRIGGERS.toString().split(", "));
        List<String> female = Arrays.asList(Settings.DEM_FEMALE.toString().split(", "));
        List<String> male = Arrays.asList(Settings.DEM_MALE.toString().split(", "));
        List<String> ignore = Arrays.asList(Settings.DEM_IGNORE.toString().split(", "));

        String[] messageA = message.toLowerCase().split("\\s+");

        boolean trigger = false;
        boolean gender = false;

        // Does message contain important data?
        for (String string : messageA) {
            if (ignore.contains(string)) {
                trigger = false;
                break;
            }
            if (triggers.contains(string)) {
                trigger = true;
            }
            if (female.contains(string) || male.contains(string)) {
                gender = true;
            }
        }

        // if not end
        if (!trigger) {
            return;
        }

        // Manage important data
        int ageNum = -1;
        for (String string : messageA) {
            try {
                ageNum = Integer.parseInt(string);
                if (ageNum != -1) {
                    break;
                }
            } catch (Exception e) {
            }
        }
        if (ageNum != -1 && ageNum < 100) {
            data.getDemData().setAge(ageNum);
        }
        if (gender) {
            for (String string : messageA) {
                if (female.contains(string)) {
                    data.getDemData().setGender(Gender.FEMALE);
                } else if (male.contains(string)) {
                    data.getDemData().setGender(Gender.MALE);
                }
            }
        }
    }

    /**
     * Locates the player upon login
     *
     * Uses ip-api.com to locate the IP address If too many calls are made to
     * the API the IP will be blocked from further calls.
     *
     * @param ip Player's IP address
     * @param data UserData corresponding the player
     */
    public void handleLogin(InetAddress ip, UserData data) {
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
