package main.java.com.djrapitops.plan.data.additional.essentials;

import main.java.com.djrapitops.plan.data.additional.Hook;
import com.earth2me.essentials.Essentials;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 * A Class responsible for hooking to Essentials and registering 3 data sources.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
public class EssentialsHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     *
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @see API
     * @throws NoClassDefFoundError when the plugin class can not be found.
     */
    public EssentialsHook(HookHandler hookH) throws NoClassDefFoundError {
        super("com.earth2me.essentials.Essentials");
        if (enabled) {
            Essentials ess = getPlugin(Essentials.class);
            hookH.addPluginDataSource(new EssentialsJailed(ess));
            hookH.addPluginDataSource(new EssentialsMuted(ess));
            hookH.addPluginDataSource(new EssentialsWarps(ess));
        }
    }
}
