package com.djrapitops.pluginbridge.plan.jobs;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A Class responsible for hooking to Jobs and registering data sources.
 *
 * @author Rsl1122
 * @since 3.2.1
 */
@Singleton
public class JobsHook extends Hook {

    private final Formatter<Double> decimalFormatter;

    @Inject
    public JobsHook(Formatters formatters) {
        super("com.gamingmesh.jobs.Jobs");
        decimalFormatter = formatters.decimals();
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            handler.addPluginDataSource(new JobsData(decimalFormatter));
        }
    }
}
