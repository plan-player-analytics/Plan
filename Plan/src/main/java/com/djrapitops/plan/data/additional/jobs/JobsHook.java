package main.java.com.djrapitops.plan.data.additional.jobs;

import main.java.com.djrapitops.plan.data.additional.Hook;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.data.additional.mcmmo.McmmoAnalysisSkillTable;

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
     * @see API
     */
    public JobsHook(HookHandler hookH) {
        super("com.gamingmesh.jobs.Jobs");
        if (enabled) {
            hookH.addPluginDataSource(new JobsInspectJobTable());
            hookH.addPluginDataSource(new JobsAnalysisJobTable());
        }
    }
}
