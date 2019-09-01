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

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.delivery.upkeep.ExportTask;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Schedules export tasks so that they are not all run at once.
 *
 * @author Rsl1122
 */
@Singleton
public class ExportScheduler {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final TaskSystem taskSystem;

    private final Exporter exporter;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    @Inject
    public ExportScheduler(
            PlanConfig config,
            DBSystem dbSystem,
            TaskSystem taskSystem,
            Exporter exporter,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.taskSystem = taskSystem;
        this.exporter = exporter;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    public void scheduleExport() {
        scheduleServerPageExport();
    }

    public void scheduleServerPageExport() {
        if (!config.get(ExportSettings.SERVER_PAGE)) return;

        Collection<Server> servers = dbSystem.getDatabase().query(ServerQueries.fetchPlanServerInformationCollection());
        int serverCount = servers.size();
        if (serverCount == 0) return;

        long period = TimeAmount.toTicks(config.get(ExportSettings.EXPORT_PERIOD), TimeUnit.MILLISECONDS);
        long offset = period / serverCount;

        Optional<Server> proxy = servers.stream().filter(Server::isProxy).findFirst();
        proxy.ifPresent(server -> taskSystem.registerTask("Server export",
                new ExportTask(exporter, exporter -> exporter.exportServerPage(server), logger, errorHandler))
                .runTaskTimerAsynchronously(0L, period)
        );

        int offsetMultiplier = proxy.isPresent() ? 1 : 0; // Delay first server export if on a network.
        for (Server server : servers) {
            taskSystem.registerTask("Server export",
                    new ExportTask(exporter, exporter -> exporter.exportServerPage(server), logger, errorHandler))
                    .runTaskTimerAsynchronously(offset * offsetMultiplier, period);
            offsetMultiplier++;
        }
    }

}