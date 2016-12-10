package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.UUIDFetcher;
import com.massivecraft.factions.Factions;
import java.util.HashMap;

import com.massivecraft.factions.entity.MPlayer;
import java.util.UUID;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.OfflinePlayer;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class FactionsHook implements Hook {

    private Plan plugin;
    private Factions factions;

    public FactionsHook(Plan plugin) throws Exception {
        this.plugin = plugin;
        this.factions = getPlugin(Factions.class);
    }

    @Override
    public HashMap<String, String> getData(String player) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        MPlayer mplayer;
        UUID uuid = UUIDFetcher.getUUIDOf(player);
        OfflinePlayer p;
        if (uuid != null) {
            p = getOfflinePlayer(uuid);
            mplayer = MPlayer.get(uuid);
        } else {
            // Fallback method if UUID is not found
            p = getOfflinePlayer(player);
            mplayer = MPlayer.get(p.getUniqueId());
        }
        // Check if player has played on server
        if (p.hasPlayedBefore()) {
            if (mplayer.hasFaction()) {
                data.put("FAC-FACTION", mplayer.getFactionName());
                if (mplayer.hasTitle()) {
                    data.put("FAC-TITLE", mplayer.getTitle());
                }
            }
            data.put("FAC-POWER", mplayer.getPowerRounded() + " / " + mplayer.getPowerMax());
            data.put("FAC-POWER PER HOUR", "" + mplayer.getPowerPerHour());
            data.put("FAC-POWER PER DEATH", "" + mplayer.getPowerPerDeath());
        }
        return data;
    }

    @Override
    public HashMap<String, String> getAllData(String player) throws Exception {
        return getData(player);
    }
}
