package com.djrapitops.pluginbridge.plan;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plugin.api.Check;

/**
 * Abstract class for easy hooking of plugins.
 *
 * @author Rsl1122
 * @since 2.6.0
 */
public abstract class Hook {

    /**
     * Is the plugin being hooked properly enabled?
     */
    protected boolean enabled;

    /**
     * Class constructor.
     * <p>
     * Checks if the given plugin (class path) is enabled.
     *
     * @param pluginClass Class path string of the plugin's main JavaPlugin class.
     */
    public Hook(String pluginClass) {
        enabled = Check.isAvailable(pluginClass);
    }

    public abstract void hook(HookHandler handler) throws NoClassDefFoundError;

    /**
     * Constructor to set enabled to false.
     */
    public Hook() {
        enabled = false;
    }
}
