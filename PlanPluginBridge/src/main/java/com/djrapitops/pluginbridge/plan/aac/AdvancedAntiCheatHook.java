/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.aac;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.pluginbridge.plan.Hook;

/**
 * Hook for AAC plugin.
 *
 * @author Rsl1122
 */
public class AdvancedAntiCheatHook extends Hook {

    private static PlayerHackKickListener listener;

    public AdvancedAntiCheatHook(HookHandler hookHandler) {
        super("me.konsolas.aac.AAC", hookHandler);
    }

    @Override
    public void hook() throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }
        Plan plugin = Plan.getInstance();

        HackerTable table = new HackerTable((SQLDB) Database.getActive());
        try {
            table.createTable();
        } catch (DBException e) {
            Log.toLog(this.getClass().getName(), e);
            return;
        }

        if (listener == null) {
            listener = new PlayerHackKickListener();
            plugin.registerListener(listener);
        }
        addPluginDataSource(new AdvancedAntiCheatData(table));
    }
}