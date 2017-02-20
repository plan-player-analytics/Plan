package main.java.com.djrapitops.plan.data.additional;

import org.bukkit.plugin.java.JavaPlugin;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public abstract class Hook {

    private boolean enabled;

    /**
     *
     * @param plugin
     */
    public Hook(Class<? extends JavaPlugin> plugin) {
        JavaPlugin hookedPlugin = getPlugin(plugin);
        try {
            enabled = hookedPlugin.isEnabled();
        } catch (Exception | NoClassDefFoundError e) {
            enabled = false;
        }
    }

    /**
     * @return Whether or not the plugin was successfully hooked.
     */
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
