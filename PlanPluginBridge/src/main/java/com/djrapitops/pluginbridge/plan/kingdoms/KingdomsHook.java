/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.kingdoms;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hook for Kingdoms and Kingdoms+ plugins.
 *
 * @author Rsl1122
 */
@Singleton
public class KingdomsHook extends Hook {

    @Inject
    public KingdomsHook() {
        super("org.kingdoms.main.Kingdoms");
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            handler.addPluginDataSource(new KingdomsData());
        }
    }
}