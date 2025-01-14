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

import com.djrapitops.plan.component.ComponentSvc;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.implementation.CallerImplementation;
import com.djrapitops.plan.extension.implementation.ExtensionMethodErrorTracker;
import com.djrapitops.plan.extension.implementation.ExtensionRegister;
import com.djrapitops.plan.extension.implementation.ExtensionWrapper;
import com.djrapitops.plan.extension.implementation.builder.ExtDataBuilder;
import com.djrapitops.plan.extension.implementation.providers.gathering.DataValueGatherer;
import com.djrapitops.plan.extension.implementation.providers.gathering.GraphSamplers;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.UUIDUtility;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.ExtensionSettings;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation for {@link ExtensionService}.
 *
 * @author AuroraLS3
 */
@Singleton
public class ExtensionSvc implements ExtensionService {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ComponentSvc componentService;
    private final ServerInfo serverInfo;
    private final Processing processing;
    private final ExtensionRegister extensionRegister;
    private final GraphSamplers graphSamplers;
    private final UUIDUtility uuidUtility;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    private final Map<String, DataValueGatherer> extensionGatherers;
    private final AtomicBoolean enabled;

    @Inject
    public ExtensionSvc(
            PlanConfig config,
            DBSystem dbSystem,
            ComponentSvc componentService,
            ServerInfo serverInfo,
            Processing processing,
            ExtensionRegister extensionRegister,
            GraphSamplers graphSamplers,
            UUIDUtility uuidUtility,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.componentService = componentService;
        this.serverInfo = serverInfo;
        this.processing = processing;
        this.extensionRegister = extensionRegister;
        this.graphSamplers = graphSamplers;
        this.uuidUtility = uuidUtility;
        this.logger = logger;
        this.errorLogger = errorLogger;

        extensionGatherers = new HashMap<>();
        enabled = new AtomicBoolean(true);
    }

    public void register() {
        Holder.set(this);
    }

    public void registerExtensions() {
        try {
            enabled.set(true);
            extensionRegister.registerBuiltInExtensions(config.getExtensionSettings().getDisabled());
        } catch (IllegalStateException failedToRegisterOne) {
            ErrorContext.Builder context = ErrorContext.builder()
                    .whatToDo("Report and/or disable the failed extensions. You can find the failed extensions in the error file.");
            for (Throwable suppressedException : failedToRegisterOne.getSuppressed()) {
                context.related(suppressedException.getMessage());
            }

            logger.warn("One or more extensions failed to register (They can be disabled in Plan config).");
            errorLogger.warn(failedToRegisterOne, context.build());
        }
    }

    @Override
    public Optional<Caller> register(DataExtension dataExtension) {
        ExtensionWrapper extension = new ExtensionWrapper(dataExtension);
        String pluginName = extension.getPluginName();

        if (shouldNotAllowRegistration(pluginName)) return Optional.empty();

        for (String warning : extension.getWarnings()) {
            logger.warn("DataExtension API implementation mistake for " + pluginName + ": " + warning);
        }

        DataValueGatherer gatherer = new DataValueGatherer(extension, dbSystem, componentService, serverInfo, errorLogger);
        gatherer.storeExtensionInformation();
        extensionGatherers.put(pluginName, gatherer);
        graphSamplers.registerGraphSamplers(extension);
        graphSamplers.storePlayerGraphMetadata(extension);

        processing.submitNonCritical(() -> updateServerValues(gatherer, CallEvents.SERVER_EXTENSION_REGISTER));

        logger.info("Registered extension: " + pluginName);
        return Optional.of(new CallerImplementation(gatherer, this, processing));
    }

    @Override
    public void unregister(DataExtension extension) {
        extensionGatherers.remove(extension.getPluginName());
    }

    @Override
    public ExtensionDataBuilder newExtensionDataBuilder(DataExtension extension) {
        return new ExtDataBuilder(extension);
    }

    private boolean shouldNotAllowRegistration(String pluginName) {
        ExtensionSettings pluginsConfig = config.getExtensionSettings();

        if (!pluginsConfig.hasSection(pluginName)) {
            try {
                pluginsConfig.createSection(pluginName);
            } catch (IOException e) {
                errorLogger.warn(e, ErrorContext.builder()
                        .whatToDo("Create 'Plugins." + pluginName + ".Enabled: true' setting manually.")
                        .related("Section: " + pluginName).build());
                logger.warn("Could not register DataExtension for " + pluginName + " due to " + e.toString());
                return true;
            }
        }

        // Should the extension not be registered?
        return !pluginsConfig.isEnabled(pluginName);
    }

    public void updatePlayerValues(UUID playerUUID, String playerName, CallEvents event) {
        if (!enabled.get()) return; // Plugin is disabling
        for (DataValueGatherer gatherer : extensionGatherers.values()) {
            if (event == CallEvents.PLAYER_JOIN) {
                graphSamplers.registerPlayerGraphSamplers(gatherer.getExtension(), playerUUID, playerName);
            } else if (event == CallEvents.PLAYER_LEAVE) {
                graphSamplers.unregisterPlayerSamplers(playerUUID);
            }
            updatePlayerValues(gatherer, playerUUID, playerName, event);
        }
    }

    public void updatePlayerValues(DataValueGatherer gatherer, UUID playerUUID, String playerName, CallEvents event) {
        if (!enabled.get()) return; // Plugin is disabling
        if (gatherer.shouldSkipEvent(event)) return;
        if (playerUUID == null && playerName == null) return;

        UUID realUUID = playerUUID != null ? playerUUID : uuidUtility.getUUIDOf(playerName);
        if (realUUID == null) return;

        String realPlayerName = playerName != null ?
                playerName :
                uuidUtility.getNameOf(realUUID).orElse(null);

        gatherer.updateValues(realUUID, realPlayerName);
    }

    public void updateServerValues(CallEvents event) {
        if (!enabled.get()) return; // Plugin is disabling
        for (DataValueGatherer gatherer : extensionGatherers.values()) {
            updateServerValues(gatherer, event);
        }
    }

    public void updateServerValues(DataValueGatherer gatherer, CallEvents event) {
        if (!enabled.get()) return; // Plugin is disabling
        if (gatherer.shouldSkipEvent(event)) return;

        gatherer.updateValues();
    }

    public void disableUpdates() {
        enabled.set(false);
        ExtensionMethodErrorTracker.clear();
    }
}