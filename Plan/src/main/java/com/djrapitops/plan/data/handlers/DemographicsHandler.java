package com.djrapitops.plan.data.handlers;

import com.djrapitops.plan.data.cache.DataCacheHandler;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.Gender;
import com.djrapitops.plan.data.DemographicsData;
import com.djrapitops.plan.data.UserData;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author Rsl1122
 */
public class DemographicsHandler {

    private final DataCacheHandler handler;

    /**
     * Class Constructor
     *
     * @param plugin Current instance of Plan.class
     * @param h Current instance of DataCacheHandler h
     */
    public DemographicsHandler(Plan plugin, DataCacheHandler h) {
        this.handler = h;
    }

    /**
     * Checks the message for Demographics relevant data
     *
     * If message contains triggerwords and words that define data important,
     * informatino will be saved in the DemographicsData of UserData provided
     *
     * @param event The Chat event passed by listener
     * @param data UserData corresponding to player of this event.
     */
    public void handleChatEvent(AsyncPlayerChatEvent event, UserData data) {
        List<String> triggers = Arrays.asList("i\'m", "am", "im");
        List<String> female = Arrays.asList("female", "girl", "gurl", "woman", "gal", "mrs", "she", "miss");
        List<String> male = Arrays.asList("male", "boy", "man", "boe", "sir", "mr", "guy", "he");
        List<String> ignore = Arrays.asList("sure", "think", "with", "are");

        String message = event.getMessage();
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
     * @param event JoinEvent to get the InetAddress
     * @param data UserData corresponding the player
     */
    public void handleLogin(PlayerJoinEvent event, UserData data) {
        InetAddress address = event.getPlayer().getAddress().getAddress();
        DemographicsData demData = data.getDemData();
        try {
            String result = "";
            URL url = new URL("http://freegeoip.net/csv/" + address.getHostAddress());
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
            demData.setGeoLocation("Not Known");
        }
    }
}
