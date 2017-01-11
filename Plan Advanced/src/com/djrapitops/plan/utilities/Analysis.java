package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class Analysis {

    private Plan plugin;
    private AnalysisData data;
    private InspectCacheHandler inspectCache;
    private final List<UserData> rawData;
    private final List<UUID> added;

    public Analysis(Plan plugin) {
        this.plugin = plugin;
        this.inspectCache = plugin.getInspectCache();
        rawData = new ArrayList<>();
        added = new ArrayList<>();
    }

    public void analyze() {
        rawData.clear();
        added.clear();
        plugin.log("Analysis | Beginning analysis of user data..");
        OfflinePlayer[] offlinePlayers = Bukkit.getServer().getOfflinePlayers();
        List<UUID> uuids = new ArrayList<>();
        for (OfflinePlayer p : offlinePlayers) {
            UUID uuid = p.getUniqueId();
            if (plugin.getDB().wasSeenBefore(uuid)) {
                uuids.add(uuid);
            }
        }
        (new BukkitRunnable() {
            @Override
            public void run() {
                uuids.stream().forEach((uuid) -> {
                    inspectCache.cache(uuid);
                });
                plugin.log("Analysis | Fetching Data..");
                while (rawData.size() != uuids.size()) {
                    try {
                        this.wait(1);
                    } catch (InterruptedException ex) {
                    }
                    uuids.stream()
                            .filter((uuid) -> (!added.contains(uuid)))
                            .forEach((uuid) -> {
                        UserData userData = inspectCache.getFromCache(uuid);
                        if (userData != null) {
                            rawData.add(userData);
                            added.add(uuid);
                        }
                    });
                }
                plugin.log("Analysis | Data Fetched, beginning Analysis of data..");
                
            }
        }).runTaskAsynchronously(plugin);
    }
}
