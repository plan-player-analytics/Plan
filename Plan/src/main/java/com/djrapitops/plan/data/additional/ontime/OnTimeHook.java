package main.java.com.djrapitops.plan.data.additional.ontime;

import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.data.additional.Hook;
import main.java.com.djrapitops.plan.data.additional.HookHandler;

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
    public OnTimeHook(HookHandler hookH) throws NoClassDefFoundError {
        super("me.edge209.OnTime.OnTime");
        if (enabled) {
            hookH.addPluginDataSource(new OntimeVotes());
            hookH.addPluginDataSource(new OntimeVotesWeek());
            hookH.addPluginDataSource(new OntimeVotesMonth());
            hookH.addPluginDataSource(new OntimeRefer());
            hookH.addPluginDataSource(new OntimeReferWeek());
            hookH.addPluginDataSource(new OntimeReferMonth());
        }
    }
}
