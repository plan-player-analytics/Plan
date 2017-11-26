package com.djrapitops.pluginbridge.plan.essentials;

import main.java.com.djrapitops.plan.data.additional.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import com.earth2me.essentials.Essentials;
import main.java.com.djrapitops.plan.api.API;

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
     * <p>
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @throws NoClassDefFoundError when the plugin class can not be found.
     * @see API
     */
    public EssentialsHook(HookHandler hookH) {
        super("com.earth2me.essentials.Essentials", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            Essentials ess = getPlugin(Essentials.class);
            addPluginDataSource(new EssentialsData(ess));
        }
    }
}
