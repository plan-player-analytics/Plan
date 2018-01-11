package com.djrapitops.pluginbridge.plan.mcmmo;

import com.djrapitops.pluginbridge.plan.Hook;
import com.djrapitops.plan.api.API;
import com.djrapitops.plan.data.plugin.HookHandler;

/**
 * A Class responsible for hooking to MCMMO and registering data sources.
 *
 * @author Rsl1122
 * @since 3.2.1
 */
public class McmmoHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     *
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @see API
     */
    public McmmoHook(HookHandler hookH) {
        super("com.gmail.nossr50.mcMMO", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            addPluginDataSource(new McMmoData());
        }
    }
}
