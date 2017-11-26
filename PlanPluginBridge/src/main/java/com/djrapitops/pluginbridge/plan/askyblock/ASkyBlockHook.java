package com.djrapitops.pluginbridge.plan.askyblock;

import com.djrapitops.pluginbridge.plan.Hook;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.data.additional.HookHandler;

/**
 * A Class responsible for hooking to ASkyBlock and registering data sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class ASkyBlockHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     * <p>
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @throws NoClassDefFoundError when the plugin class can not be found.
     * @see API
     */
    public ASkyBlockHook(HookHandler hookH) throws NoClassDefFoundError {
        super("com.wasteofplastic.askyblock.ASkyBlock", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            ASkyBlockAPI api = ASkyBlockAPI.getInstance();
            addPluginDataSource(new ASkyBlockData(api));
        }
    }
}
