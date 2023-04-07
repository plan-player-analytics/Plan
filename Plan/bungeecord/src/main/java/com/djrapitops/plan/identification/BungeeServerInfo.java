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
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import net.playeranalytics.plugin.server.PluginLogger;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Manages Server information on the Bungee instance.
 *
 * @author AuroraLS3
 */
@Singleton
public class BungeeServerInfo extends ServerInfo {

    private final String currentVersion;
    private final ServerLoader fromFile;
    private final ServerLoader fromDatabase;

    private final Processing processing;
    private final Addresses addresses;

    private final Locale locale;
    private final PluginLogger logger;

    @Inject
    public BungeeServerInfo(
            @Named("currentVersion") String currentVersion,
            ServerProperties serverProperties,
            ServerFileLoader fromFile,
            ServerDBLoader fromDatabase,
            Processing processing,
            Addresses addresses,
            Locale locale,
            PluginLogger logger
    ) {
        super(serverProperties);
        this.currentVersion = currentVersion;
        this.fromFile = fromFile;
        this.fromDatabase = fromDatabase;
        this.processing = processing;
        this.addresses = addresses;
        this.locale = locale;
        this.logger = logger;
    }

    @Override
    public void loadServerInfo() {
        logger.info(locale.getString(PluginLang.LOADING_SERVER_INFO));

        this.server = fromFile.load(null)
                .orElseGet(this::registerServer);
        this.server.setProxy(true); // Ensure isProxy if loaded from file

        processing.submitNonCritical(this::updateStorage);
    }

    private void updateStorage() {
        String address = getAddress();

        server.setWebAddress(address);

        fromDatabase.save(server);
        server = fromDatabase.load(server.getUuid()).orElse(server);
        fromFile.save(server);
    }

    /**
     * @throws EnableException If IP setting is unset
     */
    private Server registerServer() {
        Server proxy = createServerObject();

        fromDatabase.save(proxy);
        Server stored = fromDatabase.load(proxy.getUuid())
                .orElseThrow(() -> new EnableException("Server registration to database failed"));

        fromFile.save(stored);
        return stored;
    }

    private Server createServerObject() {
        ServerUUID serverUUID = generateNewUUID();
        String accessAddress = getAddress();
        return new Server(-1, serverUUID, "BungeeCord", accessAddress, true, currentVersion);
    }

    @Nullable
    private String getAddress() {
        return addresses.getAccessAddress()
                .orElse(addresses.isWebserverEnabled() ? addresses.getFallbackLocalhostAddress() : null);
    }
}
