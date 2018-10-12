package com.djrapitops.pluginbridge.plan.litebans;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.pluginbridge.plan.Hook;
import litebans.api.Database;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Class responsible for hooking to LiteBans and registering data
 * sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class LiteBansBungeeHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     * <p>
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @throws NoClassDefFoundError when the plugin class can not be found.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LiteBansBungeeHook(HookHandler hookH) {
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
            LiteBansDatabaseQueries db = new LiteBansDatabaseQueries(getTablePrefix());
            addPluginDataSource(new LiteBansData(db));
        }
    }

    private String getTablePrefix() {
        String tablePrefix = "libeans_";
        try {
            File litebansDataFolder = ProxyServer.getInstance().getPluginManager().getPlugin("LiteBans").getDataFolder();
            File configFile = new File(litebansDataFolder, "config.yml");

            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            tablePrefix = configuration.getString("sql.table_prefix");
        } catch (NullPointerException | IOException e) {
            Logger.getLogger("Plan").log(Level.WARNING, "Could not get Litebans table prefix, using default (litebans_). " + e.toString());
        }
        return tablePrefix;
    }
}
