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
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Connection system for Bukkit servers.
 *
 * @author Rsl1122
 */
@Singleton
public class ServerConnectionSystem extends ConnectionSystem {

    private final Processing processing;
    private final DBSystem dbSystem;

    private long latestServerMapRefresh;

    private Server mainServer;

    @Inject
    public ServerConnectionSystem(
            Processing processing,
            DBSystem dbSystem,
            ServerInfo serverInfo
    ) {
        super(serverInfo);
        this.processing = processing;
        this.dbSystem = dbSystem;
        latestServerMapRefresh = 0;
    }

    private void refreshServerMap() {
        processing.submitNonCritical(() -> {
            if (latestServerMapRefresh < System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(15L)) {

                Database database = dbSystem.getDatabase();
                Map<UUID, Server> servers = database.query(ServerQueries.fetchPlanServerInformation());
                Optional<Server> proxy = servers.values().stream()
                        .filter(Server::isProxy)
                        .findFirst();
                mainServer = proxy.orElse(null);

                proxy.ifPresent(proxyServer -> servers.remove(proxyServer.getUuid()));
                latestServerMapRefresh = System.currentTimeMillis();
            }
        });
    }

    @Override
    public boolean isServerAvailable() {
        refreshServerMap();
        return mainServer != null;
    }

    @Override
    public String getMainAddress() {
        refreshServerMap();
        return isServerAvailable() ? mainServer.getWebAddress() : serverInfo.getServer().getWebAddress();

    }
}
