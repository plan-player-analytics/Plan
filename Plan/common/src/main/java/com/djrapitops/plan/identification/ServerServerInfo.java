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
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages the Server UUID for Bukkit servers.
 * <p>
 * Also manages Server ID required for MySQL database independence.
 *
 * @author Rsl1122
 */
@Singleton
public class ServerServerInfo extends ServerInfo {

    private final ServerLoader fromFile;
    private final ServerLoader fromDatabase;

    private final PlanConfig config;
    private final Processing processing;
    private final Addresses addresses;

    @Inject
    public ServerServerInfo(
            ServerProperties serverProperties,
            ServerFileLoader fromFile,
            ServerDBLoader fromDatabase,
            Processing processing,
            PlanConfig config,
            Addresses addresses
    ) {
        super(serverProperties);
        this.fromFile = fromFile;
        this.fromDatabase = fromDatabase;
        this.processing = processing;
        this.addresses = addresses;
        this.config = config;
    }

    @Override
    protected void loadServerInfo() {
        Optional<Server> loaded = fromFile.load(null);
        server = loaded.orElseGet(this::registerNew);
        processing.submitNonCritical(this::updateStorage);
    }

    private void updateStorage() {
        String address = addresses.getAccessAddress().orElseGet(addresses::getFallbackLocalhostAddress);
        String name = config.get(PluginSettings.SERVER_NAME);

        server.setName(name);
        server.setWebAddress(address);

        fromDatabase.save(server);
        server = fromDatabase.load(server.getUuid()).orElse(server);
        fromFile.save(server);
    }

    private Server registerNew() {
        return registerNew(generateNewUUID());
    }

    private Server registerNew(UUID serverUUID) {
        Server server = createServerObject(serverUUID);
        fromDatabase.save(server);

        Server stored = fromDatabase.load(serverUUID)
                .orElseThrow(() -> new EnableException("Failed to register server (not found after saving to database)"));
        fromFile.save(stored);
        return stored;
    }

    private Server createServerObject(UUID serverUUID) {
        String webAddress = addresses.getAccessAddress().orElseGet(addresses::getFallbackLocalhostAddress);
        String name = config.get(PluginSettings.SERVER_NAME);
        return new Server(serverUUID, name, webAddress);
    }
}