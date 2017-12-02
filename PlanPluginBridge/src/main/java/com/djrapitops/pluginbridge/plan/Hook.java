package com.djrapitops.pluginbridge.plan;

import main.java.com.djrapitops.plan.data.plugin.HookHandler;
import main.java.com.djrapitops.plan.data.plugin.PluginData;
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

    protected HookHandler hookHandler;

    private Hook() {
        throw new IllegalStateException();
    }

    /**
     * Class constructor.
     * <p>
     * Checks if the given plugin (class path) is enabled.
     *
     * @param plugin Class path string of the plugin's main JavaPlugin class.
     */
    public Hook(String plugin, HookHandler hookHandler) {
        this.hookHandler = hookHandler;
        try {
            Class<?> givenClass = Class.forName(plugin);
            Class<? extends JavaPlugin> pluginClass = (Class<? extends JavaPlugin>) givenClass;
            JavaPlugin hookedPlugin = getPlugin(pluginClass);
            enabled = hookedPlugin.isEnabled();
        } catch (NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError | Exception e) {
            enabled = false;
        }
    }

    public abstract void hook() throws NoClassDefFoundError;

    /**
     * Constructor to set enabled to false.
     */
    public Hook(HookHandler hookHandler) {
        enabled = false;
        this.hookHandler = hookHandler;
    }

    protected void addPluginDataSource(PluginData pluginData) {
        hookHandler.addPluginDataSource(pluginData);
    }
}
