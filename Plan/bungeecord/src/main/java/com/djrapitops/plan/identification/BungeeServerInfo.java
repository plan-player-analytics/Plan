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
package com.djrapitops.plan.identification;

import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.delivery.webserver.WebServer;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.properties.ServerProperties;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plugin.logging.console.PluginLogger;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Manages Server information on the Bungee instance.
 *
 * @author Rsl1122
 */
@Singleton
public class BungeeServerInfo extends ServerInfo {

    private final DBSystem dbSystem;
    private final Addresses addresses;
    private final Lazy<WebServer> webServer;
    private final PluginLogger logger;

    @Inject
    public BungeeServerInfo(
            ServerProperties serverProperties,
            DBSystem dbSystem,
            Addresses addresses,
            Lazy<WebServer> webServer,
            PluginLogger logger
    ) {
        super(serverProperties);
        this.dbSystem = dbSystem;
        this.addresses = addresses;
        this.webServer = webServer;
        this.logger = logger;
    }

    @Override
    public void loadServerInfo() throws EnableException {
        checkIfDefaultIP();

        try {
            Database database = dbSystem.getDatabase();
            Optional<Server> proxyInfo = database.query(ServerQueries.fetchProxyServerInformation());
            if (proxyInfo.isPresent()) {
                server = proxyInfo.get();
                updateServerInfo();
            } else {
                server = registerBungeeInfo(database);
            }
        } catch (DBOpException | ExecutionException e) {
            throw new EnableException("Failed to read Server information from Database.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateServerInfo() {
        addresses.getAccessAddress().ifPresent(this::saveAddress);
    }

    private void saveAddress(String accessAddress) {
        if (!accessAddress.equals(server.getWebAddress())) {
            server.setWebAddress(accessAddress);
            dbSystem.getDatabase().executeTransaction(new StoreServerInformationTransaction(server));
        }
    }

    private void checkIfDefaultIP() throws EnableException {
        String ip = serverProperties.getIp();
        if ("0.0.0.0".equals(ip)) {
            logger.error("IP setting still 0.0.0.0 - Configure Alternative_IP/IP that connects to the Proxy server.");
            logger.info("Player Analytics partially enabled (Use /planbungee to reload config)");
            throw new EnableException("IP setting still 0.0.0.0 - Configure Alternative_IP/IP that connects to the Proxy server.");
        }
    }

    private Server registerBungeeInfo(Database db) throws EnableException, ExecutionException, InterruptedException {
        UUID serverUUID = generateNewUUID();
        String accessAddress = addresses.getAccessAddress().orElseThrow(() -> new EnableException("BungeeCord can not have '0.0.0.0' or '' as an address. Set up 'Server.IP' setting."));

        Server proxy = new Server(-1, serverUUID, "BungeeCord", accessAddress, serverProperties.getMaxPlayers());
        db.executeTransaction(new StoreServerInformationTransaction(proxy))
                .get();

        Optional<Server> proxyInfo = db.query(ServerQueries.fetchProxyServerInformation());
        if (proxyInfo.isPresent()) {
            return proxyInfo.get();
        }
        throw new EnableException("BungeeCord registration failed (Was not found in the database after saving)");
    }
}
