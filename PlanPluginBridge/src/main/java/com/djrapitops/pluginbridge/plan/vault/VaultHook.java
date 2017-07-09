package com.djrapitops.pluginbridge.plan.vault;

import com.djrapitops.pluginbridge.plan.Hook;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.api.API;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import static org.bukkit.Bukkit.getServer;

/**
 * A Class responsible for hooking to Vault and registering data sources.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
public class VaultHook extends Hook {

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
            Permission permSys = getServer().getServicesManager().getRegistration(Permission.class).getProvider();
            hookH.addPluginDataSource(new PermGroup(permSys));
            hookH.addPluginDataSource(new PermGroupTable(permSys));
        } catch (Throwable e) {
        }
        
        try {
            Economy econ = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
            hookH.addPluginDataSource(new EconomyBalance(econ));
            hookH.addPluginDataSource(new EconomyBalanceTable(econ));
        } catch (Throwable e) {
        }
    }
}
