package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.api.Hook;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.UUIDFetcher;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.api.DataType;
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
    public HashMap<String, DataPoint> getData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        UUID uuid = UUIDFetcher.getUUIDOf(player);
        OfflinePlayer p = getOfflinePlayer(uuid);
        if (p.hasPlayedBefore()) {
            data.put("ECO-BALANCE", new DataPoint(this.econ.format(this.econ.getBalance(p)), DataType.AMOUNT_WITH_LETTERS));
        }
        return data;
    }

    @Override
    public HashMap<String, DataPoint> getAllData(String player) throws Exception {
        return getData(player);
    }

}
