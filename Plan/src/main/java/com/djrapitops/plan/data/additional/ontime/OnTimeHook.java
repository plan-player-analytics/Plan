package main.java.com.djrapitops.plan.data.additional.ontime;

import main.java.com.djrapitops.plan.data.additional.Hook;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.API;

/**
 *
 * @author Rsl1122
 */
public class OnTimeHook extends Hook {


    /**
     * Hooks to OnTime plugin
     */
    public OnTimeHook() throws NoClassDefFoundError {
        super("me.edge209.OnTime.OnTime");
        if (enabled) {
            API planAPI = Plan.getPlanAPI();
            planAPI.addPluginDataSource(new OntimeVotes());
            planAPI.addPluginDataSource(new OntimeVotesWeek());
            planAPI.addPluginDataSource(new OntimeVotesMonth());
            planAPI.addPluginDataSource(new OntimeRefer());
            planAPI.addPluginDataSource(new OntimeReferWeek());
            planAPI.addPluginDataSource(new OntimeReferMonth());
            
        }
    }
}
