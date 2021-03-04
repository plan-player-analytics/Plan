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
package com.djrapitops.plan.delivery.export;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * System in charge of exporting html.
 *
 * @author AuroraLS3
 */
@Singleton
public class ExportSystem implements SubSystem {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final ExportScheduler exportScheduler;

    @Inject
    public ExportSystem(
            DBSystem dbSystem,
            ServerInfo serverInfo,
            ExportScheduler exportScheduler
    ) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.exportScheduler = exportScheduler;
    }

    @Override
    public void enable() {
        Database database = dbSystem.getDatabase();
        boolean hasProxy = database.query(ServerQueries.fetchProxyServerInformation()).isPresent();
        if (serverInfo.getServer().isNotProxy() && hasProxy) {
            return;
        }

        exportScheduler.scheduleExport();
    }

    @Override
    public void disable() {
        // Nothing to disable
    }
}