package com.djrapitops.pluginbridge.plan.litebans;

import com.djrapitops.pluginbridge.plan.Hook;
import litebans.api.Database;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.data.additional.HookHandler;

/**
 * A Class responsible for hooking to LiteBans and registering data
 * sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class LiteBansHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     * <p>
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @throws NoClassDefFoundError when the plugin class can not be found.
     * @see API
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LiteBansHook(HookHandler hookH) throws NoClassDefFoundError {
        super();
        try {
            Database.get();
            enabled = true;
        } catch (NoSuchFieldError | NoSuchMethodError | Exception e) {
            enabled = false;
        }
        if (enabled) {
            LiteBansDatabaseQueries db = new LiteBansDatabaseQueries();
            hookH.addPluginDataSource(new LiteBansBansTable(db));
            hookH.addPluginDataSource(new LiteBansInspectBansTable(db));
        }
    }
}
