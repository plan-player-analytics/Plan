package com.djrapitops.pluginbridge.plan.litebans;

import com.djrapitops.pluginbridge.plan.Hook;
import litebans.api.Database;
import com.djrapitops.plan.api.API;
import com.djrapitops.plan.data.plugin.HookHandler;

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
    public LiteBansHook(HookHandler hookH) {
        super(hookH);
        try {
            Database.get();
            enabled = true;
        } catch (NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError | Exception e) {
            enabled = false;
        }
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            LiteBansDatabaseQueries db = new LiteBansDatabaseQueries();
            addPluginDataSource(new LiteBansData(db));
        }
    }
}
