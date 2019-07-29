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
package com.djrapitops.pluginbridge.plan.protocolsupport;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hook for ProtocolSupport plugin.
 *
 * @author Rsl1122
 */
@Singleton
public class ProtocolSupportHook extends Hook {

    private final Plan plugin;
    private final DBSystem dbSystem;

    @Inject
    public ProtocolSupportHook(
            Plan plugin,
            DBSystem dbSystem
    ) {
        super("protocolsupport.ProtocolSupport");
        this.plugin = plugin;
        this.dbSystem = dbSystem;
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }
        Database database = dbSystem.getDatabase();
        database.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(ProtocolTable.createTableSQL(database.getType()));
            }
        });

        plugin.registerListener(new PlayerVersionListener(database));
        handler.addPluginDataSource(new ProtocolSupportData(database));
    }
}