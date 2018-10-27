package com.djrapitops.pluginbridge.plan.litebans;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;
import litebans.api.Database;

import javax.inject.Inject;
import javax.inject.Singleton;

import static github.scarsz.discordsrv.util.PluginUtil.getPlugin;

/**
 * A Class responsible for hooking to LiteBans and registering data
 * sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
@Singleton
public class LiteBansBukkitHook extends Hook {

    private Formatter<Long> timestampFormatter;

    @Inject
    public LiteBansBukkitHook(
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
            String tablePrefix = getPlugin("LiteBans").getConfig().getString("sql.table_prefix");
            LiteBansDatabaseQueries db = new LiteBansDatabaseQueries(tablePrefix);
            handler.addPluginDataSource(new LiteBansData(db, timestampFormatter));
        }
    }
}
