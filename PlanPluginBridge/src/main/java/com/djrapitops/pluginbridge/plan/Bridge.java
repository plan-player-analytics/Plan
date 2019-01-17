package com.djrapitops.pluginbridge.plan;

import com.djrapitops.plan.data.plugin.HookHandler;

/**
 * Interface for Hooking into other plugins.
 *
 * @author Rsl1122
 */
public interface Bridge {
    void hook(HookHandler handler);
}
