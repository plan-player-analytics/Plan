/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.buycraft;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.pluginbridge.plan.Hook;

/**
 * Hook for BuyCraft plugin.
 *
 * @author Rsl1122
 */
public class BuyCraftHook extends Hook {

    private final String secret;

    public BuyCraftHook(HookHandler hookHandler) {
        super(hookHandler);

        secret = Settings.PLUGIN_BUYCRAFT_SECRET.toString();
        enabled = !secret.equals("-") && !secret.isEmpty();
    }

    @Override
    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            hookHandler.addPluginDataSource(new BuyCraftPluginData(secret));
        }
    }
}