package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.api.Hook;
import com.djrapitops.plan.PlanLite;
import com.djrapitops.plan.UUIDFetcher;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.api.DataType;
import java.util.HashMap;
import java.util.UUID;

import me.BadBones69.Logger.SettingsManager;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import static org.bukkit.Bukkit.getOfflinePlayer;

public class PlayerLoggerHook implements Hook {

    private PlanLite plugin;

    public PlayerLoggerHook(PlanLite plugin) throws Exception, NoClassDefFoundError {
        this.plugin = plugin;
        SettingsManager.getInstance();
    }

    @Override
    public HashMap<String, DataPoint> getData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        FileConfiguration file = SettingsManager.getInstance().getData();
        try {
            UUID uuid = UUIDFetcher.getUUIDOf(player);
            OfflinePlayer p = getOfflinePlayer(uuid);
            if (p.hasPlayedBefore()) {
                data.put("PLG-REGISTERED", new DataPoint(file.getString("Players." + uuid + ".DateJoined"), DataType.DEPRECATED));
                data.put("PLG-LAST LOGIN", new DataPoint(file.getString("Players." + uuid + ".LastSeen"), DataType.DEPRECATED));
                data.put("PLG-TIMES JOINED", new DataPoint(file.getString("Players." + uuid + ".TimePlayed"), DataType.AMOUNT));
                data.put("PLG-KILLS", new DataPoint(file.getString("Players." + uuid + ".Kills"), DataType.AMOUNT));
                data.put("PLG-DEATHS", new DataPoint(file.getString("Players." + uuid + ".Deaths"), DataType.AMOUNT));
                data.put("PLG-TIMES KICKED", new DataPoint(file.getString("Players." + uuid + ".Kicks"), DataType.AMOUNT));
            }
        } catch (IllegalArgumentException e) {
        }
        return data;
    }

    @Override
    public HashMap<String, DataPoint> getAllData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        data.putAll(getData(player));
        try {
            UUID uuid = UUIDFetcher.getUUIDOf(player);
            OfflinePlayer p = getOfflinePlayer(uuid);
            FileConfiguration file = SettingsManager.getInstance().getData();
            if (p.hasPlayedBefore()) {
                data.put("PLG-STICKS MADE", new DataPoint(file.getString("Players." + uuid + ".Sticks"), DataType.AMOUNT));
                data.put("PLG-STEPS", new DataPoint(file.getString("Players." + uuid + ".Steps"), DataType.AMOUNT));
                data.put("PLG-CROUCHES", new DataPoint(file.getString("Players." + uuid + ".Twerks"), DataType.AMOUNT));
            }
        } catch (IllegalArgumentException e) {
        }
        return data;
    }

}
