package main.java.com.djrapitops.plan.data.additional.essentials;

import main.java.com.djrapitops.plan.data.additional.Hook;
import com.earth2me.essentials.Essentials;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class EssentialsHook extends Hook {

    /**
     * Hooks to Essentials plugin
     *
     */
    public EssentialsHook(HookHandler hookH) throws NoClassDefFoundError{
        super("com.earth2me.essentials.Essentials");
        if (enabled) {
            Essentials ess = getPlugin(Essentials.class);
            hookH.addPluginDataSource(new EssentialsJailed(ess));
            hookH.addPluginDataSource(new EssentialsMuted(ess));
            hookH.addPluginDataSource(new EssentialsWarps(ess));
        }
    }
}
