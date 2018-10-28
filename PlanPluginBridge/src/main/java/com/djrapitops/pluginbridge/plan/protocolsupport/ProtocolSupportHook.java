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
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.pluginbridge.plan.Hook;
import com.djrapitops.pluginbridge.plan.viaversion.ProtocolTable;

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
    private final Processing processing;
    private final DBSystem dbSystem;

    @Inject
    public ProtocolSupportHook(
            Plan plugin,
            Processing processing,
            DBSystem dbSystem
    ) {
        super("protocolsupport.ProtocolSupport");
        this.plugin = plugin;
        this.processing = processing;
        this.dbSystem = dbSystem;
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }
        ProtocolTable protocolTable = new ProtocolTable((SQLDB) dbSystem.getDatabase());
        try {
            protocolTable.createTable();
        } catch (DBException e) {
            throw new DBOpException("Failed to create Protocol table", e);
        }
        plugin.registerListener(new PlayerVersionListener(processing, protocolTable));
        handler.addPluginDataSource(new ProtocolSupportData(protocolTable));
    }
}