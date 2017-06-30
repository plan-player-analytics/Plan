package com.djrapitops.pluginbridge.plan.viaversion;

import com.djrapitops.pluginbridge.plan.Hook;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.api.API;
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

    /**
     * Hooks the plugin and registers it's PluginData objects.
     *
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @see API
     * @throws NoClassDefFoundError when the plugin class can not be found.
     */
    public ViaVersionHook(HookHandler hookH) throws NoClassDefFoundError {
        super("us.myles.ViaVersion.ViaVersionPlugin");
        if (!enabled) {
            return;
        }
        Plan plan = Plan.getInstance();
        ViaAPI api = Via.getAPI();
        ProtocolTable table = new ProtocolTable((SQLDB) plan.getDB());
        table.createTable();
        PlayerVersionListener l = new PlayerVersionListener(plan, api, table);
        plan.registerListener(l);
        hookH.addPluginDataSource(new ViaVersionVersionTable(table));
        hookH.addPluginDataSource(new ViaVersionVersion(table));
    }
}