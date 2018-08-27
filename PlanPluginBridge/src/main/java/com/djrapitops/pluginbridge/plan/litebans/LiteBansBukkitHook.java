package com.djrapitops.pluginbridge.plan.litebans;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.pluginbridge.plan.Hook;
import litebans.api.Database;
import org.bukkit.Bukkit;

/**
 * A Class responsible for hooking to LiteBans and registering data
 * sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class LiteBansBukkitHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     * <p>
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @throws NoClassDefFoundError when the plugin class can not be found.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LiteBansBukkitHook(HookHandler hookH) {
        super(hookH);
        try {
            Database.get();
            enabled = true;
        } catch (NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError | Exception e) {
            if (Settings.DEV_MODE.isTrue()) {
                Log.toLog(this.getClass(), e);
            }
            enabled = false;
        }
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            String tablePrefix = Bukkit.getPluginManager().getPlugin("LiteBans").getConfig().getString("sql.table_prefix");
            LiteBansDatabaseQueries db = new LiteBansDatabaseQueries(tablePrefix);
            addPluginDataSource(new LiteBansData(db));
        }
    }
}
