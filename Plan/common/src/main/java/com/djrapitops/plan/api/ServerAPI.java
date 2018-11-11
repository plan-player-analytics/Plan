/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.api;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * PlanAPI extension for Bukkit
 *
 * @author Rsl1122
 */
@Singleton
public class ServerAPI extends CommonAPI {

    private final HookHandler hookHandler;
    private final DBSystem dbSystem;

    @Inject
    public ServerAPI(
            UUIDUtility uuidUtility,
            HookHandler hookHandler,
            DBSystem dbSystem,
            ErrorHandler errorHandler
    ) {
        super(uuidUtility, errorHandler);
        this.hookHandler = hookHandler;
        this.dbSystem = dbSystem;
    }

    @Override
    public void addPluginDataSource(PluginData pluginData) {
        hookHandler.addPluginDataSource(pluginData);
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return dbSystem.getDatabase().fetch().getPlayerName(uuid);
    }

    @Override
    public FetchOperations fetchFromPlanDB() {
        return dbSystem.getDatabase().fetch();
    }
}
