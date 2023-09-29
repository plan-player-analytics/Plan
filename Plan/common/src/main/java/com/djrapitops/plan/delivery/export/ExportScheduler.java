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

import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.PluginRunnable;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Schedules export tasks so that they are not all run at once.
 *
 * @author AuroraLS3
 */
@Singleton
public class ExportScheduler extends PluginRunnable {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    private final RunnableFactory runnableFactory;
    private final Exporter exporter;
    private final ErrorLogger errorLogger;

    @Inject
    public ExportScheduler(
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            RunnableFactory runnableFactory,
            Exporter exporter,
            ErrorLogger errorLogger
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.runnableFactory = runnableFactory;
        this.exporter = exporter;
        this.errorLogger = errorLogger;
    }

    @Override
    public void run() {
        scheduleExport();
    }

    private void scheduleExport() {
        Database database = dbSystem.getDatabase();
        boolean hasProxy = !database.query(ServerQueries.fetchProxyServers()).isEmpty();
        if (serverInfo.getServer().isNotProxy() && hasProxy) {
            return;
        }

        scheduleReactExport();
        scheduleServerPageExport();
        schedulePlayersPageExport();
    }

    private void scheduleReactExport() {
        runnableFactory.create(
                new ExportTask(exporter, Exporter::exportReact, errorLogger)
        ).runTaskLaterAsynchronously(TimeAmount.toTicks(5, TimeUnit.SECONDS));
    }

    private void schedulePlayersPageExport() {
        long period = TimeAmount.toTicks(config.get(ExportSettings.EXPORT_PERIOD), TimeUnit.MILLISECONDS)
                / 4;
        runnableFactory.create(
                new ExportTask(exporter, Exporter::exportPlayersPage, errorLogger)
        ).runTaskTimerAsynchronously(TimeAmount.toTicks(2, TimeUnit.MINUTES), period);
    }

    private void scheduleServerPageExport() {
        if (config.isFalse(ExportSettings.SERVER_PAGE)) return;

        Collection<Server> servers = dbSystem.getDatabase().query(ServerQueries.fetchPlanServerInformationCollection());
        int serverCount = servers.size();
        if (serverCount == 0) return;

        long period = TimeAmount.toTicks(config.get(ExportSettings.EXPORT_PERIOD), TimeUnit.MILLISECONDS);
        long offset = period / serverCount;

        Optional<Server> proxy = servers.stream().filter(Server::isProxy).findFirst();
        proxy.ifPresent(mainServer -> runnableFactory.create(
                        new ExportTask(exporter, same -> same.exportServerPage(mainServer), errorLogger))
                .runTaskTimerAsynchronously(TimeAmount.toTicks(1, TimeUnit.MINUTES), period)
        );

        int offsetMultiplier = proxy.isPresent() ? 1 : 0; // Delay first server export if on a network.
        for (Server server : servers) {
            runnableFactory.create(
                            new ExportTask(exporter, same -> {
                                same.exportServerPage(server);
                                same.exportServerJSON(server);
                            }, errorLogger))
                    .runTaskTimerAsynchronously(offset * offsetMultiplier, period);
            offsetMultiplier++;
        }
    }

}