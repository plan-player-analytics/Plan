package com.djrapitops.pluginbridge.plan.griefprevention.plus;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import net.kaikk.mc.gpp.DataStore;
import net.kaikk.mc.gpp.GriefPreventionPlus;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 * A Class responsible for hooking to GriefPreventionPlus and registering data
 * sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class GriefPreventionPlusHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     * <p>
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @throws NoClassDefFoundError when the plugin class can not be found.
     */
    public GriefPreventionPlusHook(HookHandler hookH) {
        super("net.kaikk.mc.gpp.GriefPreventionPlus", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            DataStore dataStore = getPlugin(GriefPreventionPlus.class).getDataStore();
            addPluginDataSource(new GriefPreventionPlusData(dataStore));
        }
    }
}
