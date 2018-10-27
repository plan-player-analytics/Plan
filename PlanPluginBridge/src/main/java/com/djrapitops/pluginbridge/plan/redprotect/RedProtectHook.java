/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.redprotect;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hook for RedProtect plugin.
 *
 * @author Rsl1122
 */
@Singleton
public class RedProtectHook extends Hook {

    @Inject
    public RedProtectHook() {
        super("br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect");
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            handler.addPluginDataSource(new RedProtectData());
        }
    }
}