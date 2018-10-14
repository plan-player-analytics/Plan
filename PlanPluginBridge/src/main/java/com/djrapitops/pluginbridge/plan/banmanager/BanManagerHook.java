/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.banmanager;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hook for BanManager plugin.
 *
 * @author Rsl1122
 */
@Singleton
public class BanManagerHook extends Hook {

    private final Formatter<Long> timestampFormatter;

    @Inject
    public BanManagerHook(Formatters formatters) {
        super("me.confuser.banmanager.BanManager");
        timestampFormatter = formatters.yearLong();
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }

        handler.addPluginDataSource(new BanManagerData(timestampFormatter));
    }
}