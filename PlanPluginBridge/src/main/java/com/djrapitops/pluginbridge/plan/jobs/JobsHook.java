package com.djrapitops.pluginbridge.plan.jobs;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;

/**
 * A Class responsible for hooking to Jobs and registering data sources.
 *
 * @author Rsl1122
 * @since 3.2.1
 */
public class JobsHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     *
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     */
    public JobsHook(HookHandler hookH) {
        super("com.gamingmesh.jobs.Jobs", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            addPluginDataSource(new JobsData());
        }
    }
}
