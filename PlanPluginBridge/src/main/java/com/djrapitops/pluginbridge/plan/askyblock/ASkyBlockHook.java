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
     *
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @see API
     * @throws NoClassDefFoundError when the plugin class can not be found.
     */
    public ASkyBlockHook(HookHandler hookH) throws NoClassDefFoundError {
        super("com.wasteofplastic.askyblock.ASkyBlock");
        if (enabled) {
            ASkyBlockAPI api = ASkyBlockAPI.getInstance();
            hookH.addPluginDataSource(new ASkyBlockIslandName(api));
            hookH.addPluginDataSource(new ASkyBlockIslandLevel(api));
            hookH.addPluginDataSource(new ASkyBlockIslandResets(api));
            hookH.addPluginDataSource(new ASkyBlockIslands(api));
        }
    }
}
