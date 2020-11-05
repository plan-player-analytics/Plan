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
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.identification.properties.ServerProperties;
import com.djrapitops.plan.identification.storage.ServerDBLoader;
import com.djrapitops.plan.identification.storage.ServerFileLoader;
import com.djrapitops.plan.identification.storage.ServerLoader;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * Manages Server information on the Bungee instance.
 *
 * @author Rsl1122
 */
@Singleton
public class BungeeServerInfo extends ServerInfo {

    private final ServerLoader fromFile;
    private final ServerLoader fromDatabase;

    private final Processing processing;
    private final Addresses addresses;
    private final PluginLogger logger;

    @Inject
    public BungeeServerInfo(
            ServerProperties serverProperties,
            ServerFileLoader fromFile,
            ServerDBLoader fromDatabase,
            Processing processing,
            Addresses addresses,
            PluginLogger logger
    ) {
        super(serverProperties);
        this.fromFile = fromFile;
        this.fromDatabase = fromDatabase;
        this.processing = processing;
        this.addresses = addresses;
        this.logger = logger;
    }

    @Override
    public void loadServerInfo() {
        checkIfDefaultIP();

        this.server = fromFile.load(null).orElseGet(() -> fromDatabase.load(null)
                .orElseGet(this::registerServer));
        processing.submitNonCritical(this::updateStorage);
    }

    private void updateStorage() {
        String address = addresses.getAccessAddress().orElseGet(addresses::getFallbackLocalhostAddress);

        server.setWebAddress(address);

        fromDatabase.save(server);
        fromFile.save(server);
    }

    /**
     * @throws EnableException
     */
    private void checkIfDefaultIP() {
        String ip = serverProperties.getIp();
        if ("0.0.0.0".equals(ip)) {
            logger.error("IP setting still 0.0.0.0 - Configure Alternative_IP/IP that connects to the Proxy server.");
            logger.info("Player Analytics partially enabled (Use /planbungee to reload config)");
            throw new EnableException("IP setting still 0.0.0.0 - Configure Alternative_IP/IP that connects to the Proxy server.");
        }
    }

    /**
     * @throws EnableException
     */
    private Server registerServer() {
        Server proxy = createServerObject();

        fromDatabase.save(proxy);
        Server stored = fromDatabase.load(null)
                .orElseThrow(() -> new EnableException("BungeeCord registration failed (DB)"));

        fromFile.save(stored);
        return stored;
    }

    /**
     * @throws EnableException
     */
    private Server createServerObject() {
        UUID serverUUID = generateNewUUID();
        String accessAddress = addresses.getAccessAddress().orElseThrow(() -> new EnableException("Velocity can not have '0.0.0.0' or '' as an address. Set up 'Server.IP' setting."));
        return new Server(-1, serverUUID, "BungeeCord", accessAddress);
    }
}
