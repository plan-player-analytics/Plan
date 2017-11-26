package com.djrapitops.pluginbridge.plan.vault;

import com.djrapitops.pluginbridge.plan.Hook;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import net.milkbowl.vault.economy.Economy;

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
     * <p>
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @throws NoClassDefFoundError when the plugin class can not be found.
     * @see API
     */
    public VaultHook(HookHandler hookH) throws NoClassDefFoundError {
        super("net.milkbowl.vault.Vault", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }

//        try {
//            Permission permSys = getServer().getServicesManager().getRegistration(Permission.class).getProvider();
//            addPluginDataSource(new VaultPermData(permSys));
//        } catch (NoSuchFieldError | NoSuchMethodError | Exception e) {
//        }

        try {
            Economy econ = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
            addPluginDataSource(new VaultEcoData(econ));
        } catch (NoSuchFieldError | NoSuchMethodError | Exception e) {
        }
    }
}
