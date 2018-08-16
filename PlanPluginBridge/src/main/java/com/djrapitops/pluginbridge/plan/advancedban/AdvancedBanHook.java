/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.advancedban;

import com.djrapitops.pluginbridge.plan.Hook;
import com.djrapitops.plan.data.plugin.HookHandler;

/**
 * Hook for AdvancedBan plugin.
 *
 * @author Vankka
 */
public class AdvancedBanHook extends Hook {
    public AdvancedBanHook(HookHandler hookHandler) {
        super("me.leoko.advancedban.Universal", hookHandler);
    }

    @Override
    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            addPluginDataSource(new AdvancedBanData());
        }
    }
}
