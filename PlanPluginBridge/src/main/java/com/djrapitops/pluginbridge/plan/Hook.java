package com.djrapitops.pluginbridge.plan;

import org.bukkit.plugin.java.JavaPlugin;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

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
     *
     * Checks if the given plugin (class path) is enabled.
     *
     * @param plugin Class path string of the plugin's main JavaPlugin class.
     */
    public Hook(String plugin) {
        try {
            Class<? extends JavaPlugin> pluginClass = (Class<? extends JavaPlugin>) Class.forName(plugin);
            JavaPlugin hookedPlugin = getPlugin(pluginClass);
            enabled = hookedPlugin.isEnabled();
        } catch (Exception | NoClassDefFoundError e) {
            enabled = false;
        }
    }

    /**
     * Consturctor to set enabled to false.
     */
    public Hook() {
        enabled = false;
    }

    /**
     * Check if the hooked plugin is enabled.
     *
     * @return true/false
     */
    public boolean isEnabled() {
        return enabled;
    }
}
