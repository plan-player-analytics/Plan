package main.java.com.djrapitops.plan.data.handlers;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.utilities.PlaceholderUtils;
import com.djrapitops.planlite.UUIDFetcher;
import com.djrapitops.planlite.api.DataPoint;
import com.djrapitops.planlite.api.DataType;
import com.djrapitops.planlite.api.Hook;
import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author Rsl1122
 */
public class PlanLiteDataPushHook implements Hook {

    private final Plan plugin;
    
    public PlanLiteDataPushHook(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     * Used to send data to PlanLite if it's use as UI is enabled.
     *
     * @param playername
     * @return
     * @throws Exception
     */
    @Override
    public HashMap<String, DataPoint> getData(String playername) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        try {
            UUID uuid = UUIDFetcher.getUUIDOf(playername);
            if (uuid != null) {
                InspectCacheHandler inspectCache = plugin.getInspectCache();
                inspectCache.cache(uuid);
                UserData uData = inspectCache.getFromCache(uuid);
                HashMap<String, String> userData = PlaceholderUtils.getInspectReplaceRules(uData);
                for (String key : userData.keySet()) {
                    if (key.equals("%planlite%") || key.equals("%gmpiechart%")) {
                        continue;
                    }
                    data.put("PLA-" + key.toUpperCase().substring(1, key.length() - 1), new DataPoint(userData.get(key), DataType.OTHER));
                }
            }
        } catch (Exception e) {
        }
        return data;
    }

    /**
     * Used to send data to PlanLite if it's use as UI is enabled.
     *
     * @param playername
     * @return
     * @throws Exception
     */
    @Override
    public HashMap<String, DataPoint> getAllData(String playername) throws Exception {
        return getData(playername);
    }
}
