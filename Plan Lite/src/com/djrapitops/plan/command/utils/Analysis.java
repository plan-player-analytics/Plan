package com.djrapitops.plan.command.utils;

import com.djrapitops.plan.PlanLite;
import com.djrapitops.plan.command.hooks.AdvancedAchievementsHook;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.api.DataType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class Analysis {

    public static HashMap<String, DataPoint> analyze(HashMap<UUID, HashMap<String, DataPoint>> playerData) {
        PlanLite plugin = getPlugin(PlanLite.class);
        HashMap<String, List<String>> playerDataLists = new HashMap<>();
        HashMap<String, DataType> dataTypes = new HashMap<>();
        // Ignore following keys (Strings, unprocessable or irrelevant data)
        DataType[] ignoreType = {DataType.DEPRECATED, DataType.STRING, DataType.LOCATION, DataType.LINK, DataType.HEATMAP,
            DataType.MAP, DataType.OTHER, DataType.DATE, DataType.TIME_TIMESTAMP};
        String[] ignore = {"ESS-HEALTH", "ESS-HUNGER", "ESS-XP LEVEL", "ESS-OPPED"};
        List<String> ignoreKeys = new ArrayList<>();
        List<DataType> ignoreTypes = new ArrayList<>();
        try {
            AdvancedAchievementsHook aaHook = (AdvancedAchievementsHook) plugin.getHooks().get("AdvancedAchievements");
            if (!aaHook.isUsingUUID()) {
                ignoreKeys.add("AAC-ACHIEVEMENTS");
            }
        } catch (Exception e) {
            ignoreKeys.add("AAC-ACHIEVEMENTS");
        }
        ignoreKeys.addAll(Arrays.asList(ignore));
        ignoreTypes.addAll(Arrays.asList(ignoreType));

        // Turn playerData into Hashmap of Lists sorted by keys.
        playerData.keySet().parallelStream().forEach((key) -> {
            playerData.get(key).keySet().parallelStream()
                    .filter((dataKey) -> !(ignoreKeys.contains(dataKey)))
                    .map((dataKey) -> {
                        if (dataTypes.get(dataKey) == null) {
                            dataTypes.put(dataKey, playerData.get(key).get(dataKey).type());
                        }
                        return dataKey;
                    })
                    .filter((dataKey) -> !(ignoreTypes.contains(dataTypes.get(dataKey))))
                    .map((dataKey) -> {
                        if (playerDataLists.get(dataKey) == null) {
                            playerDataLists.put(dataKey, new ArrayList<>());
                        }
                        return dataKey;
                    })
                    .forEach((dataKey) -> {
                        playerDataLists.get(dataKey).add(playerData.get(key).get(dataKey).data());
                    });
        });

        HashMap<String, DataPoint> analyzedData = new HashMap<>();

        // Analyze
        playerDataLists.keySet().parallelStream().forEach((dataKey) -> {
            DataType type = dataTypes.get(dataKey);
            if (type == DataType.AMOUNT
                    || type == DataType.AMOUNT_WITH_LETTERS
                    || type == DataType.AMOUNT_WITH_MAX
                    || type == DataType.PERCENT) {
                // Get a clean list of dataPoints with only numbers
                List<String> dataPoints = playerDataLists.get(dataKey);
                if (null != type) {
                    switch (type) {
                        case AMOUNT_WITH_LETTERS:
                            dataPoints = AnalysisUtils.parseWLetters(playerDataLists.get(dataKey));
                            break;
                        case PERCENT:
                            dataPoints = AnalysisUtils.parseWLetters(playerDataLists.get(dataKey));
                            break;
                        case AMOUNT_WITH_MAX:
                            dataPoints = AnalysisUtils.parseWMax(playerDataLists.get(dataKey));
                            break;
                        default:
                            break;
                    }
                }                
                if (type == DataType.PERCENT) {
                    String averageAmount = AnalysisUtils.AmountAverage(dataPoints);
                    analyzedData.put(dataKey, new DataPoint(averageAmount + "%", DataType.PERCENT));
                } else {
                    String averageAmount = AnalysisUtils.AmountAverage(dataPoints);
                    analyzedData.put(dataKey, new DataPoint(averageAmount, DataType.AMOUNT));
//                    String highestAmount = AnalysisUtils.AmountHighest(dataPoints);
//                    analyzedData.put(dataKey + " (HIGHEST)", new DataPoint(highestAmount, DataType.AMOUNT));
                }
            } else if (type == DataType.TIME) {
                String averageTime = AnalysisUtils.TimeAverage(playerDataLists.get(dataKey));
                analyzedData.put(dataKey, new DataPoint(averageTime, DataType.TIME));
            } else if (type == DataType.BOOLEAN) {
                String percent = AnalysisUtils.BooleanPercent(playerDataLists.get(dataKey));
                analyzedData.put(dataKey, new DataPoint(percent, DataType.PERCENT));
            }
        });
        return DataFormatUtils.formatAnalyzed(analyzedData);
    }
}
