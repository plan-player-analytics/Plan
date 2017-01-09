package com.djrapitops.planlite.command.hooks;

import com.djrapitops.planlite.api.Hook;
import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.UUIDFetcher;
import com.djrapitops.planlite.api.DataPoint;
import com.djrapitops.planlite.api.DataType;
import com.google.common.base.Optional;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;

public class BukkitDataHook implements Hook {

    private final PlanLite plugin;

    public BukkitDataHook(PlanLite p) throws Exception {
        plugin = p;
    }

    @Override
    public HashMap<String, DataPoint> getData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        try {
            OfflinePlayer p = getOfflinePlayer(UUIDFetcher.getUUIDOf(player));
            if (p.hasPlayedBefore()) {
                data.put("BUK-REGISTERED", new DataPoint("" + p.getFirstPlayed(), DataType.DATE));
                data.put("BUK-LAST LOGIN", new DataPoint("" + p.getLastPlayed(), DataType.DATE));
                if (p.isBanned()) {
                    data.put("BUK-BANNED", new DataPoint("" + p.isBanned(), DataType.BOOLEAN));
                }
                data.put("BUK-ONLINE", new DataPoint("" + p.isOnline(), DataType.BOOLEAN));
            }
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        return data;
    }

    @Override
    public HashMap<String, DataPoint> getAllData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        data.putAll(getData(player));
        try {
            OfflinePlayer p = getOfflinePlayer(UUIDFetcher.getUUIDOf(player));
            if (p.hasPlayedBefore()) {
                Location loc = p.getBedSpawnLocation();
                if (Optional.of(loc).isPresent()) {
                    data.put("BUK-BED LOCATION WORLD", new DataPoint(loc.getWorld().getName(), DataType.STRING));
                    data.put("BUK-BED LOCATION", new DataPoint(" X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ(), DataType.LOCATION));
                }
                data.put("BUK-UUID", new DataPoint("" + p.getUniqueId(), DataType.OTHER));
            }
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        return data;
    }

}
