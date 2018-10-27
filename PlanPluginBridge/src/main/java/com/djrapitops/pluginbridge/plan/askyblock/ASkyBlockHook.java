package com.djrapitops.pluginbridge.plan.askyblock;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;
import com.wasteofplastic.askyblock.ASkyBlockAPI;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A Class responsible for hooking to ASkyBlock and registering data sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
@Singleton
public class ASkyBlockHook extends Hook {

    private final Formatter<Double> percentageFormatter;

    @Inject
    public ASkyBlockHook(
            Formatters formatters
    ) throws NoClassDefFoundError {
        super("com.wasteofplastic.askyblock.ASkyBlock");

        percentageFormatter = formatters.percentage();
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            ASkyBlockAPI api = ASkyBlockAPI.getInstance();
            handler.addPluginDataSource(new ASkyBlockData(api, percentageFormatter));
        }
    }
}
