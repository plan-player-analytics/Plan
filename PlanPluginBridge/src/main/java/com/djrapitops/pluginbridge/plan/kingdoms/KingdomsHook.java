/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.kingdoms;

import com.djrapitops.pluginbridge.plan.Hook;
import com.djrapitops.plan.data.plugin.HookHandler;

/**
 * Hook for Kingdoms and Kingdoms+ plugins.
 *
 * @author Rsl1122
 */
public class KingdomsHook extends Hook {
    public KingdomsHook(HookHandler hookHandler) {
        super("org.kingdoms.main.Kingdoms", hookHandler);
    }

    @Override
    public void hook() throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }
        addPluginDataSource(new KingdomsData());
    }
}