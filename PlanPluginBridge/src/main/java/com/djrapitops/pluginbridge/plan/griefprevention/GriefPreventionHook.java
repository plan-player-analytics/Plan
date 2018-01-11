package com.djrapitops.pluginbridge.plan.griefprevention;

import com.djrapitops.pluginbridge.plan.Hook;
import com.djrapitops.plan.api.API;
import com.djrapitops.plan.data.plugin.HookHandler;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 * A Class responsible for hooking to GriefPrevention and registering data
 * sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class GriefPreventionHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     *
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @see API
     * @throws NoClassDefFoundError when the plugin class can not be found.
     */
    public GriefPreventionHook(HookHandler hookH) {
        super("me.ryanhamshire.GriefPrevention.GriefPrevention", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            DataStore dataStore = getPlugin(GriefPrevention.class).dataStore;
            addPluginDataSource(new GriefPreventionData(dataStore));
        }
    }
}
