/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.luckperms;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import me.lucko.luckperms.LuckPerms;

/**
 * Hook for LuckPerms plugin.
 *
 * @author Vankka
 */
public class LuckPermsHook extends Hook {
    public LuckPermsHook(HookHandler hookHandler) {
        super("me.lucko.luckperms.LuckPerms", hookHandler);
    }

    @Override
    public void hook() throws IllegalStateException {
        if (enabled) {
            addPluginDataSource(new LuckPermsData(LuckPerms.getApi()));
        }
    }
}
