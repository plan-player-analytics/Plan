package com.djrapitops.pluginbridge.plan;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

/**
 * Manages connection to other plugins.
 *
 * @author Rsl1122
 */
public abstract class Bridge {

    private final PlanConfig config;
    private final ErrorHandler errorHandler;

    Bridge(
            PlanConfig config,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.errorHandler = errorHandler;
    }

    public void hook(HookHandler handler) {
        Hook[] hooks = getHooks();
        hookInto(handler, hooks);
    }

    private void hookInto(HookHandler handler, Hook[] hooks) {
        boolean devMode = config.isTrue(Settings.DEV_MODE);
        for (Hook hook : hooks) {
            try {
                hook.hook(handler);
            } catch (Exception | NoClassDefFoundError e) {
                if (devMode) {
                    errorHandler.log(L.WARN, this.getClass(), e);
                }
            }
        }
    }

    abstract Hook[] getHooks();
}
