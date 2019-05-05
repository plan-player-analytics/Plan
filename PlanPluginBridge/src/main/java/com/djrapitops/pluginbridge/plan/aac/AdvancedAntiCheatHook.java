/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.pluginbridge.plan.aac;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.system.database.DBSystem;
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
    private final DBSystem dbSystem;
    private final Formatters formatters;

    @Inject
    public AdvancedAntiCheatHook(
            Plan plugin,
            DBSystem dbSystem,
            Formatters formatters
    ) {
        super("me.konsolas.aac.AAC");
        this.plugin = plugin;
        this.dbSystem = dbSystem;
        this.formatters = formatters;
    }

    @Override
    public void hook(HookHandler hookHandler) throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }

        Database database = dbSystem.getDatabase();
        database.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(HackerTable.createTableSQL(database.getType()));
            }
        });
        database.executeTransaction(new HackerTableMissingDateColumnPatch());

        plugin.registerListener(new PlayerHackKickListener(database));
        hookHandler.addPluginDataSource(new AdvancedAntiCheatData(database, formatters.yearLong()));
    }
}