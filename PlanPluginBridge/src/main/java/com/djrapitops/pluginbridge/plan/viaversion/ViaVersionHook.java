package com.djrapitops.pluginbridge.plan.viaversion;

import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.pluginbridge.plan.Hook;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
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
     * @see API
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
        ProtocolTable table = new ProtocolTable((SQLDB) plan.getDB());
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