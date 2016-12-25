package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.api.Hook;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.UUIDFetcher;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.api.DataType;
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
    public HashMap<String, DataPoint> getData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        MPlayer mplayer;
        UUID uuid = UUIDFetcher.getUUIDOf(player);
        OfflinePlayer p = getOfflinePlayer(uuid);
        if (p.hasPlayedBefore()) {
            mplayer = MPlayer.get(uuid);
            if (mplayer.hasFaction()) {
                data.put("FAC-FACTION", new DataPoint(mplayer.getFactionName(), DataType.STRING));
                if (mplayer.hasTitle()) {
                    data.put("FAC-TITLE", new DataPoint(mplayer.getTitle(), DataType.STRING));
                }
            }
            data.put("FAC-POWER", new DataPoint(mplayer.getPowerRounded() + " / " + mplayer.getPowerMax(), DataType.AMOUNT_WITH_MAX));
            data.put("FAC-POWER PER HOUR", new DataPoint("" + mplayer.getPowerPerHour(), DataType.AMOUNT));
            data.put("FAC-POWER PER DEATH", new DataPoint("" + mplayer.getPowerPerDeath(), DataType.AMOUNT));
        }
        return data;
    }

    @Override
    public HashMap<String, DataPoint> getAllData(String player) throws Exception {
        return getData(player);
    }
}
