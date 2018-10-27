package com.djrapitops.pluginbridge.plan.towny;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A Class responsible for hooking to Towny and registering 2 data sources.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
@Singleton
public class TownyHook extends Hook {

    private final PlanConfig config;
    private final DataCache dataCache;

    @Inject
    public TownyHook(
            PlanConfig config,
            DataCache dataCache
    ) {
        super("com.palmergames.bukkit.towny.Towny");
        this.config = config;
        this.dataCache = dataCache;
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            handler.addPluginDataSource(new TownyData(config, dataCache));
        }
    }
}
