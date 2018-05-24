package com.djrapitops.pluginbridge.plan.placeholderapi;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import me.clip.placeholderapi.PlaceholderAPI;

/**
 * A Class responsible for hooking to PlaceholderAPI.
 *
 * @author Rsl1122
 */
public class PlaceholderAPIHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     * <p>
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     */
    public PlaceholderAPIHook(HookHandler hookH) {
        super("me.clip.placeholderapi.PlaceholderAPI", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            PlaceholderAPI.unregisterPlaceholderHook("plan");
            PlaceholderAPI.registerPlaceholderHook("plan", new PlanPlaceholders());
        }
    }
}
