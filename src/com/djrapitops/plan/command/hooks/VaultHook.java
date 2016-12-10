package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.UUIDFetcher;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import net.milkbowl.vault.economy.Economy;
import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.Bukkit.getServer;

public class VaultHook implements Hook {

    private Plan plugin;
    private Economy econ;

    public VaultHook(Plan plugin) throws Exception {
        this.plugin = plugin;
        this.econ = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
    }

    @Override
    public HashMap<String, String> getData(String player) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        try {
            UUID uuid = UUIDFetcher.getUUIDOf(player);
            OfflinePlayer p;
            if (uuid != null) {
                p = getOfflinePlayer(uuid);
            } else {
                p = getOfflinePlayer(player);
            }
            if (p.hasPlayedBefore()) {
                data.put("ECO-BALANCE", this.econ.format(this.econ.getBalance(p)));
            }
        } catch (Exception e) {
            plugin.logToFile("VAULTHOOK\n" + e + "\nError player: " + player);

        }
        return data;
    }

    @Override
    public HashMap<String, String> getAllData(String player) throws Exception {
        return getData(player);
    }

}
