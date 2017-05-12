package main.java.com.djrapitops.plan.data.additional.vault;

import main.java.com.djrapitops.plan.data.additional.Hook;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import net.milkbowl.vault.economy.Economy;
import static org.bukkit.Bukkit.getServer;

/**
 *
 * @author Rsl1122
 */
public class VaultHook extends Hook {

    private Economy econ;

    /**
     * Hooks to Vault plugin
     *
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
