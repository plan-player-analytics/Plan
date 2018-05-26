package com.djrapitops.pluginbridge.plan.react;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;

/**
 * Hook in charge for hooking into React.
 *
 * @author Rsl1122
 */
public class ReactHook extends Hook {

    public ReactHook(HookHandler hookHandler) {
        super("com.volmit.react.ReactPlugin", hookHandler);
    }

    @Override
    public void hook() throws NoClassDefFoundError {
        Plan plan = Plan.getInstance();

    }
}