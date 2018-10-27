package com.djrapitops.pluginbridge.plan.mcmmo;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A Class responsible for hooking to MCMMO and registering data sources.
 *
 * @author Rsl1122
 * @since 3.2.1
 */
@Singleton
public class McmmoHook extends Hook {

    private final Formatter<Double> decimalFormatter;

    @Inject
    public McmmoHook(
            Formatters formatters
    ) {
        super("com.gmail.nossr50.mcMMO");
        decimalFormatter = formatters.decimals();
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            handler.addPluginDataSource(new McMmoData(decimalFormatter));
        }
    }
}
