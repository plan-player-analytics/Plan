/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.discordsrv;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.pluginbridge.plan.Hook;
import github.scarsz.discordsrv.DiscordSRV;

/**
 * Hook for DiscordSRV plugin.
 *
 * @author Vankka
 */
public class DiscordSRVHook extends Hook {
    public DiscordSRVHook(HookHandler hookHandler) {
        super("github.scarsz.discordsrv.DiscordSRV", hookHandler);
    }

    @Override
    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            Log.toLog("DSRV readyness: " + DiscordSRV.isReady, new Exception(DiscordSRV.isReady + " DSRV readyness"));
            addPluginDataSource(new DiscordSRVData());
        }
    }
}
