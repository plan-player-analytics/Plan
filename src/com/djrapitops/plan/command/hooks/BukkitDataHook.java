package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.api.Hook;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.UUIDFetcher;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.api.DataType;
import com.google.common.base.Optional;
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
    public HashMap<String, DataPoint> getData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        OfflinePlayer p = getOfflinePlayer(UUIDFetcher.getUUIDOf(player));
        if (p.hasPlayedBefore()) {
            data.put("BUK-REGISTERED", new DataPoint("" + p.getFirstPlayed(), DataType.DATE));
            data.put("BUK-LAST LOGIN", new DataPoint("" + p.getLastPlayed(), DataType.DATE));
            if (p.isBanned()) {
                data.put("BUK-BANNED", new DataPoint("" + p.isBanned(), DataType.BOOLEAN));
            }
            data.put("BUK-ONLINE", new DataPoint("" + p.isOnline(), DataType.BOOLEAN));
        }
        return data;
    }

    @Override
    public HashMap<String, DataPoint> getAllData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        data.putAll(getData(player));
        OfflinePlayer p = getOfflinePlayer(UUIDFetcher.getUUIDOf(player));
        if (p.hasPlayedBefore()) {
            Location loc = p.getBedSpawnLocation();
            if (Optional.of(loc).isPresent()) {
                data.put("BUK-BED LOCATION WORLD", new DataPoint(loc.getWorld().getName(), DataType.STRING));
                data.put("BUK-BED LOCATION", new DataPoint(" X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ(), DataType.LOCATION));
            }
            data.put("BUK-UUID", new DataPoint("" + p.getUniqueId(), DataType.OTHER));
        }
        return data;
    }

}
