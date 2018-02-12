/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.banmanager;

import com.djrapitops.pluginbridge.plan.Hook;
import com.djrapitops.plan.data.plugin.HookHandler;

/**
 * Hook for BanManager plugin.
 *
 * @author Rsl1122
 */
public class BanManagerHook extends Hook {

    public BanManagerHook(HookHandler hookHandler) {
        super("me.confuser.banmanager.BanManager", hookHandler);
    }

    @Override
    public void hook() throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }

        addPluginDataSource(new BanManagerData());
    }
}