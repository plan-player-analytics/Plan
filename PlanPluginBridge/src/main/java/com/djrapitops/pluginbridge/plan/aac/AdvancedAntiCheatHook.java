/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.aac;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hook for AAC plugin.
 *
 * @author Rsl1122
 */
@Singleton
public class AdvancedAntiCheatHook extends Hook {

    private final Plan plugin;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final Formatters formatters;

    @Inject
    public AdvancedAntiCheatHook(
            Plan plugin,
            Processing processing,
            DBSystem dbSystem,
            Formatters formatters
    ) {
        super("me.konsolas.aac.AAC");
        this.plugin = plugin;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.formatters = formatters;
    }

    @Override
    public void hook(HookHandler hookHandler) throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }

        HackerTable hackerTable = new HackerTable((SQLDB) dbSystem.getDatabase());
        try {
            hackerTable.createTable();
        } catch (DBInitException e) {
            throw new DBOpException("Failed to create AAC database table", e);
        }

        plugin.registerListener(new PlayerHackKickListener(hackerTable, processing));
        hookHandler.addPluginDataSource(new AdvancedAntiCheatData(hackerTable, formatters.yearLong()));
    }
}