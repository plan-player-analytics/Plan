package main.java.com.djrapitops.plan.data.additional.ontime;

import main.java.com.djrapitops.plan.data.additional.Hook;
import main.java.com.djrapitops.plan.data.additional.HookHandler;

/**
 *
 * @author Rsl1122
 */
public class OnTimeHook extends Hook {


    /**
     * Hooks to OnTime plugin
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
