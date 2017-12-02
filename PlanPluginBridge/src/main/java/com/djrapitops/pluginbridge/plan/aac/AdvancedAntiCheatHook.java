/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.aac;

import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.pluginbridge.plan.Hook;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.data.plugin.HookHandler;
import main.java.com.djrapitops.plan.database.databases.SQLDB;

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

        HackerTable table = new HackerTable((SQLDB) plugin.getDB());
        try {
            table.createTable();
        } catch (DBCreateTableException e) {
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