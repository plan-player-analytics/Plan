package com.djrapitops.plan.data.handlers;

import com.djrapitops.plan.data.cache.DataCacheHandler;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.Gender;
import com.djrapitops.plan.data.UserData;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

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
        List<String> ages = Arrays.asList("years", "year-old", "old");
        List<String> ignore = Arrays.asList("sure", "think", "with", "are");

        String message = event.getMessage();
        String[] messageA = message.split("\\s+");

        boolean trigger = false;
        boolean age = false;
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
            if (ages.contains(string)) {
                age = true;
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
        if (age) {
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
        try {
            Scanner locationScanner = new Scanner("http://ip-api.com/line/" + address.getHostAddress());
            List<String> results = new ArrayList<>();
            while (locationScanner.hasNextLine()) {
                results.add(locationScanner.nextLine());
            }
            if (results.size() >= 2) {
                data.getDemData().setGeoLocation(results.get(1));
            } else {
                data.getDemData().setGeoLocation("Not Known");
            }
        } catch (Exception e) {
            Plan plugin = getPlugin(Plan.class);
            plugin.logToFile("http://ip-api.com/line/" + address.getHostAddress());
            plugin.logToFile("" + e);
            plugin.logToFile(address.toString());
        }
    }
}
