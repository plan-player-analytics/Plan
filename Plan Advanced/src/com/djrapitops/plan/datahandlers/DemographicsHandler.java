
package com.djrapitops.plan.datahandlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.Gender;
import com.djrapitops.plan.database.UserData;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class DemographicsHandler {
    private final DataHandler handler;

    public DemographicsHandler(Plan plugin, DataHandler h) {
        this.handler = h;
    }

    public void handleChatEvent(AsyncPlayerChatEvent event, UserData data) {
        Player player = event.getPlayer();
        // Create lists
        String[] triggersA = {"i\\'m", "am", "im"};
        String[] femaleA = {"female", "girl", "gurl", "woman", "gal", "mrs", "she", "miss"};
        String[] maleA = {"male", "boy", "man", "boe", "sir", "mr", "guy", "he"};
        String[] ageA = {"years", "year-old", "old"};
        String[] ignoreA = {"sure", "think", "with", "are"};
        
        Set<String> triggers = new HashSet<>();
        Set<String> female = new HashSet<>();
        Set<String> male = new HashSet<>();
        Set<String> ages = new HashSet<>();
        Set<String> ignore = new HashSet<>();
        
        triggers.addAll(Arrays.asList(triggersA));
        female.addAll(Arrays.asList(femaleA));
        male.addAll(Arrays.asList(maleA));
        ages.addAll(Arrays.asList(ageA));
        ignore.addAll(Arrays.asList(ignoreA));
        // get message
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

    public void handleLogIn(PlayerLoginEvent event, UserData data) {
        InetAddress address = event.getAddress();
        try {
                Scanner locationScanner = new Scanner("http://ip-api.com/line/" + address.getHostAddress());
                List<String> results = new ArrayList<>();
                while (locationScanner.hasNextLine()) {
                    results.add(locationScanner.nextLine());
                }
                if (results.size() >= 2) {
                    data.getDemData().setGeoLocation(results.get(1));
                } else {
                    data.getDemData().setGeoLocation("UNKOWN");
                }
            } catch (Exception e) {
                Plan plugin = getPlugin(Plan.class);
                plugin.logToFile("http://ip-api.com/line/" + address.getHostAddress());
                plugin.logToFile("" + e);
                plugin.logToFile(address.toString());
            }
    }
    
    
}
