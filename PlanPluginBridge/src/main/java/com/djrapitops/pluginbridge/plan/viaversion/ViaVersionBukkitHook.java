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
package com.djrapitops.pluginbridge.plan.viaversion;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.pluginbridge.plan.Hook;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A Class responsible for hooking to ViaVersion and registering data sources.
 *
 * @author Rsl1122

 */
@Singleton
public class ViaVersionBukkitHook extends Hook {

    private final Plan plugin;
    private final DBSystem dbSystem;

    @Inject
    public ViaVersionBukkitHook(
            Plan plugin,
            DBSystem dbSystem
    ) {
        super("us.myles.ViaVersion.ViaVersionPlugin");
        this.plugin = plugin;
        this.dbSystem = dbSystem;
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }
        ViaAPI api = Via.getAPI();
        Database database = dbSystem.getDatabase();
        database.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(ProtocolTable.createTableSQL(database.getType()));
            }
        });

        plugin.registerListener(new BukkitPlayerVersionListener(api, database));
        handler.addPluginDataSource(new ViaVersionData(database));
    }
}