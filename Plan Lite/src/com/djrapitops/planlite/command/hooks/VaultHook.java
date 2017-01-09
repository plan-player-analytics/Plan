package com.djrapitops.planlite.command.hooks;

import com.djrapitops.planlite.api.Hook;
import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.UUIDFetcher;
import com.djrapitops.planlite.api.DataPoint;
import com.djrapitops.planlite.api.DataType;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import net.milkbowl.vault.economy.Economy;
import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.Bukkit.getServer;
import static org.bukkit.Bukkit.getOfflinePlayer;

public class VaultHook implements Hook {

    private PlanLite plugin;
    private Economy econ;

    public VaultHook(PlanLite plugin) throws Exception {
        this.plugin = plugin;
        this.econ = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
    }

    @Override
    public HashMap<String, DataPoint> getData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        try {
            UUID uuid = UUIDFetcher.getUUIDOf(player);
            OfflinePlayer p = getOfflinePlayer(uuid);
            if (p.hasPlayedBefore()) {
                data.put("ECO-BALANCE", new DataPoint(this.econ.format(this.econ.getBalance(p)), DataType.AMOUNT_WITH_LETTERS));
            }
        } catch (IllegalArgumentException e) {
        }
        return data;
    }

    @Override
    public HashMap<String, DataPoint> getAllData(String player) throws Exception {
        return getData(player);
    }

}
