package com.djrapitops.pluginbridge.plan.litebans;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;
import litebans.api.Database;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
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
@Singleton
public class LiteBansBungeeHook extends Hook {

    private Formatter<Long> timestampFormatter;

    @Inject
    public LiteBansBungeeHook(
            Formatters formatters
    ) {
        super();
        try {
            enabled = Database.get() != null;
            timestampFormatter = formatters.secondLong();
        } catch (NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError | Exception e) {
            enabled = false;
        }
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            LiteBansDatabaseQueries db = new LiteBansDatabaseQueries(getTablePrefix());
            handler.addPluginDataSource(new LiteBansData(db, timestampFormatter));
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
