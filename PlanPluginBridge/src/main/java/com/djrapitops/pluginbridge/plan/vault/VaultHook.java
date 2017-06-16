package com.djrapitops.pluginbridge.plan.vault;

import com.djrapitops.pluginbridge.plan.Hook;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.api.API;
import net.milkbowl.vault.economy.Economy;
import static org.bukkit.Bukkit.getServer;

/**
 * A Class responsible for hooking to Vault and registering 1 data source.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
public class VaultHook extends Hook {

    private Economy econ;

    /**
     * Hooks the plugin and registers it's PluginData objects.
     *
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @see API
     * @throws NoClassDefFoundError when the plugin class can not be found.
     */
    public VaultHook(HookHandler hookH) throws NoClassDefFoundError {
        super("net.milkbowl.vault.Vault");
        if (!enabled) {
            return;
        }

        try {
            this.econ = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
            enabled = true;
        } catch (Throwable e) {
            enabled = false;
        }

        if (!enabled) {
            return;
        }

        hookH.addPluginDataSource(new EconomyBalance(econ));
    }
}
