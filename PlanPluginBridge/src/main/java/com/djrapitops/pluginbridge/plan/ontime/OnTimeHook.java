package com.djrapitops.pluginbridge.plan.ontime;

import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.api.API;
import com.djrapitops.pluginbridge.plan.Hook;

/**
 * A Class responsible for hooking to OnTime and registering 6 data sources.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
public class OnTimeHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     *
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @see API
     * @throws NoClassDefFoundError when the plugin class can not be found.
     */
    public OnTimeHook(HookHandler hookH) {
        super("me.edge209.OnTime.OnTime", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            addPluginDataSource(new OntimeVotes());
            addPluginDataSource(new OntimeVotesWeek());
            addPluginDataSource(new OntimeVotesMonth());
            addPluginDataSource(new OntimeRefer());
            addPluginDataSource(new OntimeReferWeek());
            addPluginDataSource(new OntimeReferMonth());
        }
    }
}
