/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.protocolsupport;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.pluginbridge.plan.Hook;
import com.djrapitops.pluginbridge.plan.viaversion.ProtocolTable;

/**
 * Hook for ProtocolSupport plugin.
 *
 * @author Rsl1122
 */
public class ProtocolSupportHook extends Hook {

    private static PlayerVersionListener listener;

    public ProtocolSupportHook(HookHandler hookHandler) {
        super("protocolsupport.ProtocolSupport", hookHandler);
    }

    @Override
    public void hook() throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }
        Plan plan = Plan.getInstance();
        ProtocolTable table = new ProtocolTable((SQLDB) Database.getActive());
        try {
            table.createTable();
        } catch (DBException e) {
            Log.toLog(this.getClass().getName(), e);
            return;
        }
        if (listener == null) {
            listener = new PlayerVersionListener();
            plan.registerListener(listener);
        }
        addPluginDataSource(new ProtocolSupportData(table));
    }
}