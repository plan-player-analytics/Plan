package com.djrapitops.plan.command.utils;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.DataPoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class DataFormatUtils {

    // Saved in case I need these later on in development before release.
    
    @Deprecated
    public static void removeExtraDataPointsSearch(HashMap<String, DataPoint> dataMap, String[] args) {
        if (args.length <= 1) {
        }
        HashMap<String, DataPoint> returnMap = new HashMap<>();
        String errors = "FORMAT-SEARCH\n";
        for (String key : dataMap.keySet()) {
            for (String arg : args) {
                try {
                    if (key.toLowerCase().contains(arg.toLowerCase())) {
                        returnMap.put(key, dataMap.get(key));
                    }
                } catch (Exception e) {
                    if (!errors.contains(Arrays.toString(args))) {
                        errors += Arrays.toString(args) + "\n";
                    }
                    errors += (e + "\n" + key + " " + arg + "\n");
                }
            }
        }
        if (!errors.equals("FORMAT-SEARCH\n")) {
            Plan plugin = getPlugin(Plan.class);
            plugin.logToFile(errors);
        }
    }

    @Deprecated
    public static List<String[]> turnDataHashMapToSortedListOfArrays(HashMap<String, DataPoint> data) {
        List<String[]> dataList = new ArrayList<>();
        data.keySet().stream().forEach((key) -> {
            dataList.add(new String[]{key, data.get(key).data()});
        });
        Collections.sort(dataList, (String[] strings, String[] otherStrings) -> strings[0].compareTo(otherStrings[0]));
        return dataList;
    }
}
