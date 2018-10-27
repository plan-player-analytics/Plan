/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.luckperms;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import me.lucko.luckperms.LuckPerms;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hook for LuckPerms plugin.
 *
 * @author Vankka
 */
@Singleton
public class LuckPermsHook extends Hook {

    @Inject
    public LuckPermsHook() {
        super("me.lucko.luckperms.LuckPerms");
    }

    @Override
    public void hook(HookHandler handler) throws IllegalStateException {
        if (enabled) {
            handler.addPluginDataSource(new LuckPermsData(LuckPerms.getApi()));
        }
    }
}
