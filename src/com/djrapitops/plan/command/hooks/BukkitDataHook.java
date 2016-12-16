package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.UUIDFetcher;
import java.util.HashMap;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class BukkitDataHook implements Hook {

    private final Plan plugin;

    public BukkitDataHook(Plan p) throws Exception {
        plugin = p;
    }

    @Override
    public HashMap<String, String> getData(String player) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        OfflinePlayer p = getOfflinePlayer(UUIDFetcher.getUUIDOf(player));
        if (p.hasPlayedBefore()) {
            data.put("BUK-REGISTERED", "" + p.getFirstPlayed());
            data.put("BUK-LAST LOGIN", "" + p.getLastPlayed());
            if (p.isBanned()) {
                data.put("BUK-BANNED", "" + p.isBanned());
            }
            data.put("BUK-ONLINE", "" + p.isOnline());
        }
        return data;
    }

    @Override
    public HashMap<String, String> getAllData(String player) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        data.putAll(getData(player));
        OfflinePlayer p = getOfflinePlayer(UUIDFetcher.getUUIDOf(player));
        Location loc = p.getBedSpawnLocation();
        if (p.hasPlayedBefore()) {
            if (loc != null) {
                data.put("BUK-BED LOCATION WORLD", loc.getWorld().getName());
                data.put("BUK-BED LOCATION", " X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ());
            }
            data.put("BUK-UUID", "" + p.getUniqueId());
        }
        return data;
    }

}
