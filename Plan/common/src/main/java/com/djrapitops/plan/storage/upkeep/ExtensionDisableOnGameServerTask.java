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
package com.djrapitops.plan.storage.upkeep;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.extension.implementation.storage.queries.HasExtensionDataForPluginQuery;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.ExtensionSettings;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ExtensionDisableOnGameServerTask extends TaskSystem.Task {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final PluginLogger logger;

    @Inject
    public ExtensionDisableOnGameServerTask(PlanConfig config, DBSystem dbSystem, PluginLogger logger) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.logger = logger;
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        runnableFactory.create(this).runTaskAsynchronously();
    }

    @Override
    public void run() {
        String pluginName = "Litebans";
        checkAndDisableProxyExtensions(pluginName);
    }

    private void checkAndDisableProxyExtensions(String pluginName) {
        Database db = dbSystem.getDatabase();
        db.query(ServerQueries.fetchProxyServers())
                .stream()
                .map(Server::getUuid)
                .forEach(proxyUUID -> checkAndDisableProxyExtension(proxyUUID, pluginName));
    }

    private void checkAndDisableProxyExtension(ServerUUID proxyUUID, String pluginName) {
        Database db = dbSystem.getDatabase();
        ExtensionSettings extensionSettings = config.getExtensionSettings();

        boolean isInstalledOnProxy = db.query(new HasExtensionDataForPluginQuery(pluginName, proxyUUID));
        if (isInstalledOnProxy && extensionSettings.isEnabled(pluginName)) {
            extensionSettings.setEnabled(pluginName, false);
            logger.info("Set " + pluginName + " Extension as disabled in config since it is already enabled on the proxy server. This is to avoid duplicate data.");
        }
    }
}
