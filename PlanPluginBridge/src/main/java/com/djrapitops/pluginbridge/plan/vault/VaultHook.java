package com.djrapitops.pluginbridge.plan.vault;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;
import net.milkbowl.vault.economy.Economy;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.bukkit.Bukkit.getServer;

/**
 * A Class responsible for hooking to Vault and registering data sources.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
@Singleton
public class VaultHook extends Hook {

    private final DataCache dataCache;
    private final Formatter<Double> decimalFormatter;

    @Inject
    public VaultHook(
            DataCache dataCache,
            Formatters formatters
    ) throws NoClassDefFoundError {
        super("net.milkbowl.vault.Vault");

        this.dataCache = dataCache;
        decimalFormatter = formatters.decimals();
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }

        try {
            Economy econ = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
            handler.addPluginDataSource(new VaultEcoData(econ, dataCache, decimalFormatter));
        } catch (NoSuchFieldError | NoSuchMethodError | Exception ignore) {
            /* Economy service not present */
        }
    }
}
