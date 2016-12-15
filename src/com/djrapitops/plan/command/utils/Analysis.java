
package com.djrapitops.plan.command.utils;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.hooks.AdvancedAchievementsHook;
import com.djrapitops.plan.command.utils.DataFormatUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class Analysis {
    
    public static HashMap<String, String> analyze(HashMap<UUID, HashMap<String, String>> playerData) {
        Plan plugin = getPlugin(Plan.class);
        HashMap<String, List<String>> playerDataLists = new HashMap<>();
        // Ignore following keys (Strings, unprocessable or irrelevant data)
        String[] ignore = {"ESS-BAN REASON", "ESS-OPPED", "ESS-MUTE TIME", "ESS-LOCATION", "ESS-HUNGER", "ESS-LOCATION WORLD",
            "ESS-NICKNAME", "ESS-UUID", "FAC-FACTION", "ONT-LAST LOGIN", "TOW-TOWN", "TOW-REGISTERED",
            "TOW-LAST LOGIN", "TOW-OWNER OF", "TOW-PLOT PERMS", "TOW-PLOT OPTIONS", "TOW-FRIENDS", "ESS-ONLINE SINCE",
            "ESS-OFFLINE SINCE"};
        List<String> ignoreKeys = new ArrayList<>();
        try {
            AdvancedAchievementsHook aaHook = (AdvancedAchievementsHook) plugin.getHooks().get("AdvancedAchievements");
            if (!aaHook.isUsingUUID()) {
                ignoreKeys.add("AAC-ACHIEVEMENTS");
            }
        } catch (Exception e) {
            ignoreKeys.add("AAC-ACHIEVEMENTS");
        }
        ignoreKeys.addAll(Arrays.asList(ignore));

        // Turn playerData into Hashmap of Lists sorted by keys.
        playerData.keySet().parallelStream().forEach((key) -> {
            playerData.get(key).keySet().parallelStream()
                    .filter((dataKey) -> !(ignoreKeys.contains(dataKey)))
                    .map((dataKey) -> {
                        if (playerDataLists.get(dataKey) == null) {
                            playerDataLists.put(dataKey, new ArrayList<>());
                        }
                        return dataKey;
                    })
                    .forEach((dataKey) -> {
                        playerDataLists.get(dataKey).add(playerData.get(key).get(dataKey));
                    });
        });

        // Define analysis method for keys
        String[] numbers = {"AAC-ACHIEVEMENTS", "ESS-HEALTH", "ESS-XP LEVEL", "FAC-POWER", "FAC-POWER PER HOUR",
            "FAC-POWER PER DEATH", "SVO-VOTES", "ONT-TOTAL VOTES", "ONT-TOTAL REFERRED", "ECO-BALANCE"};
        String[] booleanValues = {"ESS-BANNED", "ESS-JAILED", "ESS-MUTED", "ESS-FLYING", "TOW-ONLINE"};
        String[] timeValues = {"ONT-TOTAL PLAY"};

        List<String> numberKeys = new ArrayList<>();
        List<String> boolKeys = new ArrayList<>();
        List<String> timeKeys = new ArrayList<>();

        numberKeys.addAll(Arrays.asList(numbers));
        boolKeys.addAll(Arrays.asList(booleanValues));
        timeKeys.addAll(Arrays.asList(timeValues));

        // Attempt to determine if undefined data is usable
        List<String> unusedKeys = new ArrayList<>();
        unusedKeys.addAll(playerDataLists.keySet());
        unusedKeys.removeAll(numberKeys);
        unusedKeys.removeAll(boolKeys);
        unusedKeys.removeAll(timeKeys);
        unusedKeys.removeAll(ignoreKeys);
        for (String key : unusedKeys) {
            try {
                Double.parseDouble(playerDataLists.get(key).get(0));
                numberKeys.add(key);
                continue;
            } catch (Exception e) {
                
            }
            try {
                Boolean.parseBoolean(playerDataLists.get(key).get(0));
                boolKeys.add(key);
            } catch (Exception e) {
                
            }
        }
        
        HashMap<String, String> averagesAndPercents = new HashMap<>();
        int errors = 0;
        HashSet<String> errorTypes = new HashSet<>();

        // Analyze - Go through each key - Go through each point of data in the list.
        for (String dataKey : playerDataLists.keySet()) {
            if (numberKeys.contains(dataKey)) {
                double sum = 0;

                for (String dataPoint : playerDataLists.get(dataKey)) {
                    // Special cases separated.
                    try {
                        if (dataKey.equals("FAC-POWER") || dataKey.equals("AAC-ACHIEVEMENTS")) {
                            sum += Double.parseDouble(dataPoint.split(" ")[0]);
                        } else if (dataKey.equals("ECO-BALANCE")) {
                            sum += Double.parseDouble(DataFormatUtils.removeLetters(dataPoint));
                        } else {
                            sum += Double.parseDouble(dataPoint);
                        }
                    } catch (Exception e) {
                        errors++;
                        errorTypes.add("" + e);
                    }
                }
                // Average
                averagesAndPercents.put(dataKey, "" + (sum * 1.0 / playerDataLists.get(dataKey).size()));

            } else if (boolKeys.contains(dataKey)) {
                int amount = 0;
                for (String dataPoint : playerDataLists.get(dataKey)) {
                    try {
                        if (Boolean.parseBoolean(dataPoint)) {
                            amount++;
                        }
                    } catch (Exception e) {
                        errors++;
                        errorTypes.add("" + e);
                    }
                }
                // Percent
                averagesAndPercents.put(dataKey, "" + ((amount * 1.0 / playerDataLists.get(dataKey).size()) * 100) + "%");
            } else if (timeKeys.contains(dataKey)) {
                Long time = Long.parseLong("0");
                for (String dataPoint : playerDataLists.get(dataKey)) {
                    try {
                        time += Long.parseLong(dataPoint);
                    } catch (Exception e) {
                        errors++;
                        errorTypes.add("" + e);
                    }
                }
                // Average
                averagesAndPercents.put(dataKey, "" + (time * 1.0 / playerDataLists.get(dataKey).size()));
            }
        }
        // Log errors
        if (errors > 0) {
            String log = "ANALYZE\n" + errors + " error(s) occurred while analyzing total data.\nFollowing types:";
            for (String errorType : errorTypes) {
                log += "\n  " + errorType;
            }
            plugin.logToFile(log);
        }
        return DataFormatUtils.formatAnalyzed(averagesAndPercents);
    }
}
