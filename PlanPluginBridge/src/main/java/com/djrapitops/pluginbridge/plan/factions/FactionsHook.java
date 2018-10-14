package com.djrapitops.pluginbridge.plan.factions;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A Class responsible for hooking to Factions and registering 4 data sources.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
@Singleton
public class FactionsHook extends Hook {

    private final PlanConfig config;
    private final Formatters formatters;

    @Inject
    public FactionsHook(
            PlanConfig config,
            Formatters formatters
    ) {
        super("com.massivecraft.factions.Factions");
        this.config = config;
        this.formatters = formatters;
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            handler.addPluginDataSource(new FactionsData(config, formatters.yearLong(), formatters.decimals()));
        }
    }
}
