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
package com.djrapitops.plan.settings.upkeep;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.StoreConfigTransaction;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * Task that stores a server config in the database on boot.
 *
 * @author AuroraLS3
 */
@Singleton
public class ConfigStoreTask extends TaskSystem.Task {

    private final PlanFiles files;
    private final PlanConfig config;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final PluginLogger logger;

    @Inject
    public ConfigStoreTask(
            PlanFiles files,
            PlanConfig config,
            ServerInfo serverInfo,
            DBSystem dbSystem,
            PluginLogger logger
    ) {
        this.files = files;
        this.config = config;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.logger = logger;
    }

    @Override
    public void run() {
        long lastModified = files.getConfigFile().lastModified();
        dbSystem.getDatabase().executeTransaction(new StoreConfigTransaction(serverInfo.getServerUUID(), config, lastModified));
        logger.debug("Config Store Task - Config in db now up to date.");
        cancel();
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        long delay = TimeAmount.toTicks(config.get(TimeSettings.CONFIG_UPDATE_INTERVAL), TimeUnit.MILLISECONDS) + 40;
        runnableFactory.create(null, this).runTaskLaterAsynchronously(delay);
    }
}