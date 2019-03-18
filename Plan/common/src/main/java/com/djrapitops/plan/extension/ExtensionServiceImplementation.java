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
package com.djrapitops.plan.extension;

import com.djrapitops.plan.extension.implementation.DataProviderExtractor;
import com.djrapitops.plan.extension.implementation.providers.gathering.ProviderValueGatherer;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation for {@link ExtensionService}.
 *
 * @author Rsl1122
 */
@Singleton
public class ExtensionServiceImplementation implements ExtensionService {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    private final Map<String, ProviderValueGatherer> extensionGatherers;

    @Inject
    public ExtensionServiceImplementation(
            DBSystem dbSystem,
            ServerInfo serverInfo,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.logger = logger;
        this.errorHandler = errorHandler;

        extensionGatherers = new HashMap<>();

        ExtensionService.ExtensionServiceHolder.set(this);
    }

    @Override
    public void register(DataExtension extension) {
        DataProviderExtractor extractor = new DataProviderExtractor(extension);
        ProviderValueGatherer gatherer = new ProviderValueGatherer(extension, extractor, dbSystem, serverInfo, logger);
        gatherer.storeExtensionInformation();
        extensionGatherers.put(extractor.getPluginName(), gatherer);
    }

    public void updatePlayerValues(UUID playerUUID, String playerName) {
        for (Map.Entry<String, ProviderValueGatherer> gatherer : extensionGatherers.entrySet()) {
            try {
                gatherer.getValue().updateValues(playerUUID, playerName);
            } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
                logger.warn(gatherer.getKey() + " ran into " + e.getClass().getSimpleName() + " when updating value for '" + playerName + "', reason: '" + e.getMessage() + "', stack trace to follow:");
                errorHandler.log(L.WARN, gatherer.getValue().getClass(), e);
            }
        }
    }
}