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
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.domain.PluginMetadata;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.PluginMetadataQueries;
import com.djrapitops.plan.storage.database.sql.tables.PluginVersionTable;
import com.djrapitops.plan.storage.database.transactions.events.StorePluginVersionsTransaction;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Gathers information about plugins that have been installed and their version history.
 *
 * @author AuroraLS3
 */
@Singleton
public class InstalledPluginGatheringTask extends TaskSystem.Task {

    private final ServerSensor<?> serverSensor;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;

    @Inject
    public InstalledPluginGatheringTask(ServerSensor<?> serverSensor, ServerInfo serverInfo, DBSystem dbSystem) {
        this.serverSensor = serverSensor;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        runnableFactory.create(this)
                .runTaskLaterAsynchronously(20, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        List<PluginMetadata> installedPlugins = serverSensor.getInstalledPlugins();

        ServerUUID serverUUID = serverInfo.getServerUUID();
        List<PluginMetadata> previouslyInstalledPlugins = dbSystem.getDatabase()
                .query(PluginMetadataQueries.getInstalledPlugins(serverUUID));

        List<PluginMetadata> newPlugins = new ArrayList<>();
        List<PluginMetadata> updatedPlugins = new ArrayList<>();

        Set<String> installedPluginNames = new HashSet<>();
        for (PluginMetadata installedPlugin : installedPlugins) {
            installedPluginNames.add(installedPlugin.getName());

            Optional<PluginMetadata> match = previouslyInstalledPlugins.stream()
                    .filter(plugin -> plugin.getName().equals(installedPlugin.getName()))
                    .findFirst();
            if (match.isEmpty()) {
                // New plugins are installedPlugins missing from previous list
                newPlugins.add(installedPlugin);
            } else {
                PluginMetadata previousVersion = match.get();
                String installedVersion = StringUtils.truncate(installedPlugin.getVersion(), PluginVersionTable.MAX_VERSION_LENGTH);
                if (!installedVersion.equals(previousVersion.getVersion())) {
                    // Updated plugins are plugins in the list with different version
                    updatedPlugins.add(installedPlugin);
                }
            }
        }

        // Removed plugins are previously installed plugins missing from installed list
        List<PluginMetadata> removedPlugins = previouslyInstalledPlugins.stream()
                .map(PluginMetadata::getName)
                .filter(pluginName -> !installedPluginNames.contains(pluginName))
                // Uninstalled plugin version is marked as null
                .map(pluginName -> new PluginMetadata(pluginName, null))
                .collect(Collectors.toList());

        long enableTime = PlanSystem.getServerEnableTime();
        List<PluginMetadata> pluginChangeList = new ArrayList<>();
        pluginChangeList.addAll(newPlugins);
        pluginChangeList.addAll(updatedPlugins);
        pluginChangeList.addAll(removedPlugins);

        dbSystem.getDatabase().executeTransaction(new StorePluginVersionsTransaction(enableTime, serverUUID, pluginChangeList));
    }
}
