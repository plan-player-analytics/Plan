package com.djrapitops.pluginbridge.plan.factions;

import com.djrapitops.pluginbridge.plan.Hook;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.data.plugin.HookHandler;

/**
 * A Class responsible for hooking to Factions and registering 4 data sources.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
public class FactionsHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     *
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @see API
     */
    public FactionsHook(HookHandler hookH) {
        super("com.massivecraft.factions.Factions", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            addPluginDataSource(new FactionsData());
        }
    }
}
