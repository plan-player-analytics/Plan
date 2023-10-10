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
import com.djrapitops.plan.identification.storage.AtomicServerLoader;
import com.djrapitops.plan.identification.storage.ServerDBLoader;
import com.djrapitops.plan.identification.storage.ServerFileLoader;
import com.djrapitops.plan.identification.storage.ServerLoader;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Manages the Server UUID for Bukkit servers.
 * <p>
 * Also manages Server ID required for MySQL database independence.
 *
 * @author AuroraLS3
 */
@Singleton
public class ServerServerInfo extends ServerInfo {

    private final String currentVersion;

    private final ServerLoader fromFile;
    private final ServerLoader fromDatabase;

    private final PlanConfig config;
    private final Processing processing;
    private final Addresses addresses;

    private final Locale locale;
    private final PluginLogger logger;

    @Inject
    public ServerServerInfo(
            @Named("currentVersion") String currentVersion,
            ServerProperties serverProperties,
            ServerFileLoader fromFile,
            ServerDBLoader fromDatabase,
            Processing processing,
            PlanConfig config,
            Addresses addresses,
            Locale locale,
            PluginLogger logger
    ) {
        super(serverProperties);
        this.currentVersion = currentVersion;
        this.fromFile = new AtomicServerLoader(fromFile);
        this.fromDatabase = new AtomicServerLoader(fromDatabase);
        this.processing = processing;
        this.addresses = addresses;
        this.config = config;
        this.locale = locale;
        this.logger = logger;
    }

    @Override
    protected void loadServerInfo() {
        logger.info(locale.getString(PluginLang.LOADING_SERVER_INFO));
        Optional<Server> loaded = fromFile.load(null);
        server = loaded.orElseGet(this::registerNew);
        logger.info(locale.getString(PluginLang.LOADED_SERVER_INFO, server.getUuid().toString()));
        processing.submitNonCritical(this::updateStorage);
    }

    private void updateStorage() {
        String address = getAddress();
        String name = config.get(PluginSettings.SERVER_NAME);

        server.setName(name);
        server.setWebAddress(address);

        fromDatabase.save(server);
        server = fromDatabase.load(server.getUuid()).orElse(server);
        fromFile.save(server);
    }

    private String getAddress() {
        return addresses.getAccessAddress()
                .orElse(addresses.isWebserverEnabled() ? addresses.getFallbackLocalhostAddress() : null);
    }

    private Server registerNew() {
        return registerNew(generateNewUUID());
    }

    private Server registerNew(ServerUUID serverUUID) {
        Server server = createServerObject(serverUUID);
        logger.info("Registering a new server in database with UUID " + serverUUID);
        fromDatabase.save(server);

        Server stored = fromDatabase.load(serverUUID)
                .orElseThrow(() -> new EnableException("Failed to register server (not found after saving to database)"));
        fromFile.save(stored);
        return stored;
    }

    private Server createServerObject(ServerUUID serverUUID) {
        String webAddress = getAddress();
        String name = config.get(PluginSettings.SERVER_NAME);
        return new Server(serverUUID, name, webAddress, currentVersion);
    }
}