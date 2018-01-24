package com.djrapitops.pluginbridge.plan.viaversion;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.database.DBCreateTableException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.pluginbridge.plan.Hook;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;

/**
 * A Class responsible for hooking to ViaVersion and registering data sources.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
public class ViaVersionHook extends Hook {

    private static PlayerVersionListener listener;

    /**
     * Hooks the plugin and registers it's PluginData objects.
     * <p>
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     */
    public ViaVersionHook(HookHandler hookH) {
        super("us.myles.ViaVersion.ViaVersionPlugin", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }
        Plan plan = Plan.getInstance();
        ViaAPI api = Via.getAPI();
        ProtocolTable table = new ProtocolTable((SQLDB) Database.getActive());
        try {
            table.createTable();
        } catch (DBCreateTableException e) {
            Log.toLog(this.getClass().getName(), e);
            return;
        }
        if (listener == null) {
            listener = new PlayerVersionListener(api);
            plan.registerListener(listener);
        }
        addPluginDataSource(new ViaVersionData(table));
    }
}