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

import com.djrapitops.plan.exceptions.DataExtensionMethodCallException;
import com.djrapitops.plan.extension.implementation.CallerImplementation;
import com.djrapitops.plan.extension.implementation.DataProviderExtractor;
import com.djrapitops.plan.extension.implementation.ExtensionRegister;
import com.djrapitops.plan.extension.implementation.providers.gathering.ProviderValueGatherer;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.system.DebugChannels;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.config.ExtensionSettings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation for {@link ExtensionService}.
 *
 * @author Rsl1122
 */
@Singleton
public class ExtensionServiceImplementation implements ExtensionService {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Processing processing;
    private final ExtensionRegister extensionRegister;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    private final Map<String, ProviderValueGatherer> extensionGatherers;

    @Inject
    public ExtensionServiceImplementation(
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Processing processing,
            ExtensionRegister extensionRegister,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.processing = processing;
        this.extensionRegister = extensionRegister;
        this.logger = logger;
        this.errorHandler = errorHandler;

        extensionGatherers = new HashMap<>();

        ExtensionService.ExtensionServiceHolder.set(this);
    }

    public void register() {
        try {
            extensionRegister.registerBuiltInExtensions(config.getExtensionSettings().getDisabled());
        } catch (IllegalStateException failedToRegisterOne) {
            logger.warn("One or more extensions failed to register, see suppressed exceptions.");
            errorHandler.log(L.WARN, this.getClass(), failedToRegisterOne);
        }
    }

    @Override
    public Optional<Caller> register(DataExtension extension) {
        DataProviderExtractor extractor = new DataProviderExtractor(extension);
        String pluginName = extractor.getPluginName();

        if (shouldNotAllowRegistration(pluginName)) return Optional.empty();

        for (String warning : extractor.getWarnings()) {
            logger.warn("DataExtension API implementation mistake for " + pluginName + ": " + warning);
        }

        ProviderValueGatherer gatherer = new ProviderValueGatherer(extension, extractor, dbSystem, serverInfo);
        gatherer.storeExtensionInformation();
        extensionGatherers.put(pluginName, gatherer);

        processing.submitNonCritical(() -> updateServerValues(gatherer, CallEvents.SERVER_EXTENSION_REGISTER));

        logger.info("Registered extension: " + pluginName);
        return Optional.of(new CallerImplementation(gatherer, this, processing));
    }

    @Override
    public void unregister(DataExtension extension) {
        DataProviderExtractor extractor = new DataProviderExtractor(extension);
        String pluginName = extractor.getPluginName();
        if (extensionGatherers.remove(pluginName) != null) {
            logger.getDebugLogger().logOn(DebugChannels.DATA_EXTENSIONS, pluginName + " extension unregistered.");
        }
    }

    private boolean shouldNotAllowRegistration(String pluginName) {
        ExtensionSettings pluginsConfig = config.getExtensionSettings();

        if (!pluginsConfig.hasSection(pluginName)) {
            try {
                pluginsConfig.createSection(pluginName);
            } catch (IOException e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
                logger.warn("Could not register DataExtension for " + pluginName + " due to " + e.toString());
                return true;
            }
        }

        if (!pluginsConfig.isEnabled(pluginName)) {
            logger.getDebugLogger().logOn(DebugChannels.DATA_EXTENSIONS, pluginName + " extension disabled in the config.");
            return true;
        }
        return false; // Should register.
    }

    public void updatePlayerValues(UUID playerUUID, String playerName, CallEvents event) {
        for (ProviderValueGatherer gatherer : extensionGatherers.values()) {
            updatePlayerValues(gatherer, playerUUID, playerName, event);
        }
    }

    public void updatePlayerValues(ProviderValueGatherer gatherer, UUID playerUUID, String playerName, CallEvents event) {
        if (!gatherer.canCallEvent(event)) {
            return;
        }
        try {
            logger.getDebugLogger().logOn(DebugChannels.DATA_EXTENSIONS, "Gathering values for: " + playerName);

            gatherer.updateValues(playerUUID, playerName);

            logger.getDebugLogger().logOn(DebugChannels.DATA_EXTENSIONS, "Gathering completed:  " + playerName);
        } catch (DataExtensionMethodCallException methodCallFailed) {
            logFailure(playerName, methodCallFailed);
            methodCallFailed.getMethod().ifPresent(gatherer::disableMethodFromUse);
            // Try again
            updatePlayerValues(gatherer, playerUUID, playerName, event);
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError unexpectedError) {
            logger.warn("Encountered unexpected error with " + gatherer.getPluginName() + " Extension (please report this): " + unexpectedError +
                    " (but failed safely) when updating value for '" + playerName +
                    "', stack trace to follow:");
            errorHandler.log(L.WARN, gatherer.getClass(), unexpectedError);
        }
    }

    private void logFailure(String playerName, DataExtensionMethodCallException methodCallFailed) {
        Throwable cause = methodCallFailed.getCause();
        String causeName = cause.getClass().getSimpleName();
        logger.warn("Encountered " + causeName + " with " + methodCallFailed.getPluginName() + " Extension (please report this)" +
                " (failed safely) when updating value for '" + playerName +
                "', the method was disabled temporarily (won't be called until next Plan reload)" +
                ", stack trace to follow:");
        errorHandler.log(L.WARN, getClass(), cause);
    }

    public void updateServerValues(CallEvents event) {
        for (ProviderValueGatherer gatherer : extensionGatherers.values()) {
            updateServerValues(gatherer, event);
        }
    }

    public void updateServerValues(ProviderValueGatherer gatherer, CallEvents event) {
        if (!gatherer.canCallEvent(event)) {
            return;
        }
        try {
            logger.getDebugLogger().logOn(DebugChannels.DATA_EXTENSIONS, "Gathering values for server");

            gatherer.updateValues();

            logger.getDebugLogger().logOn(DebugChannels.DATA_EXTENSIONS, "Gathering completed for server");
        } catch (DataExtensionMethodCallException methodCallFailed) {
            logFailure("server", methodCallFailed);
            methodCallFailed.getMethod().ifPresent(gatherer::disableMethodFromUse);
            // Try again
            updateServerValues(gatherer, event);
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError unexpectedError) {
            logger.warn("Encountered unexpected error with " + gatherer.getPluginName() + " Extension (please report this): " + unexpectedError +
                    " (failed safely) when updating value for server, stack trace to follow:");
            errorHandler.log(L.WARN, gatherer.getClass(), unexpectedError);
        }
    }
}