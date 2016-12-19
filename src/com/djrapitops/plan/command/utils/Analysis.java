package com.djrapitops.plan.command.utils;

import com.djrapitops.plan.Plan;
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
        Plan plugin = getPlugin(Plan.class);
        HashMap<String, List<String>> playerDataLists = new HashMap<>();
        HashMap<String, DataType> dataTypes = new HashMap<>();
        // Ignore following keys (Strings, unprocessable or irrelevant data)
        DataType[] ignoreType = {DataType.DEPRECATED, DataType.STRING, DataType.LOCATION, DataType.LINK, DataType.HEATMAP,
            DataType.MAP, DataType.OTHER, DataType.DATE};
        String[] ignore = {"ESS-ONLINE SINCE", "ESS-OFFLINE SINCE", "ESS-HEALTH", "ESS-HUNGER", "ESS-XP LEVEL", "ESS-OPPED"};
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
            if (null != dataTypes.get(dataKey)) {
                switch (dataTypes.get(dataKey)) {
                    case AMOUNT:
                        analyzedData.put(dataKey, new DataPoint(AnalysisUtils.AmountAverage(playerDataLists.get(dataKey)), DataType.AMOUNT));
                        break;
                    case AMOUNT_WITH_LETTERS:
                        analyzedData.put(dataKey, new DataPoint(AnalysisUtils.AmountWLettersAverage(playerDataLists.get(dataKey)), DataType.AMOUNT));
                        break;
                    case AMOUNT_WITH_MAX:
                        analyzedData.put(dataKey, new DataPoint(AnalysisUtils.AmountWMaxAverage(playerDataLists.get(dataKey)), DataType.AMOUNT));
                        break;
                    case TIME:
                        analyzedData.put(dataKey, new DataPoint(AnalysisUtils.TimeAverage(playerDataLists.get(dataKey)), DataType.TIME));
                        break;
                    case BOOLEAN:
                        analyzedData.put(dataKey, new DataPoint(AnalysisUtils.BooleanPercent(playerDataLists.get(dataKey)), DataType.PERCENT));
                        break;
                    case PERCENT:
                        analyzedData.put(dataKey, new DataPoint(AnalysisUtils.AmountWLettersAverage(playerDataLists.get(dataKey))+"%", DataType.PERCENT));
                        break;
                    default:
                        break;
                }
            }
        });
        return DataFormatUtils.formatAnalyzed(analyzedData);
    }
}
