/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.nucleus;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;

/**
 * Hook for AdvancedBan plugin.
 *
 * @author Vankka
 */
public class NucleusHook extends Hook {
    public NucleusHook(HookHandler hookHandler) {
        super("io.github.nucleuspowered.nucleus.NucleusPlugin", hookHandler);
    }

    @Override
    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            addPluginDataSource(new NucleusData());
        }
    }
}
