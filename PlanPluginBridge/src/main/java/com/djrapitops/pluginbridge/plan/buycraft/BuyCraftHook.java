/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.buycraft;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hook for BuyCraft plugin.
 *
 * @author Rsl1122
 */
@Singleton
public class BuyCraftHook extends Hook {

    private final String secret;
    private final PlanConfig config;
    private final Formatters formatters;

    @Inject
    public BuyCraftHook(
            PlanConfig config,
            Formatters formatters
    ) {
        super();
        this.config = config;
        this.formatters = formatters;

        secret = config.getString(Settings.PLUGIN_BUYCRAFT_SECRET);
        enabled = !secret.equals("-") && !secret.isEmpty();
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            handler.addPluginDataSource(new BuyCraftData(secret, config, formatters.yearLong(), formatters.decimals()));
        }
    }
}