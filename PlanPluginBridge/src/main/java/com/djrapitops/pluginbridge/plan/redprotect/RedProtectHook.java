/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.redprotect;

import com.djrapitops.pluginbridge.plan.Hook;
import com.djrapitops.plan.data.plugin.HookHandler;

/**
 * Hook for RedProtect plugin.
 *
 * @author Rsl1122
 */
public class RedProtectHook extends Hook {

    public RedProtectHook(HookHandler hookHandler) {
        super("br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect", hookHandler);
    }

    @Override
    public void hook() throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }

        addPluginDataSource(new RedProtectData());
    }
}