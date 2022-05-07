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
package com.djrapitops.plan;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.delivery.DeliveryUtilities;
import com.djrapitops.plan.delivery.export.ExportSystem;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.web.ResolverSvc;
import com.djrapitops.plan.delivery.web.ResourceSvc;
import com.djrapitops.plan.delivery.webserver.NonProxyWebserverDisableChecker;
import com.djrapitops.plan.delivery.webserver.WebServerSystem;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.gathering.cache.CacheSystem;
import com.djrapitops.plan.gathering.importing.ImportSystem;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.query.QuerySvc;
import com.djrapitops.plan.settings.ConfigSystem;
import com.djrapitops.plan.settings.ListenerSvc;
import com.djrapitops.plan.settings.SchedulerSvc;
import com.djrapitops.plan.settings.SettingsSvc;
import com.djrapitops.plan.settings.locale.LocaleSystem;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plan.version.VersionChecker;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * PlanSystem contains everything Plan needs to run.
 * <p>
 * This is an abstraction layer on top of Plugin instances so that tests can be run with less mocks.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlanSystem implements SubSystem {

    private static final long SERVER_ENABLE_TIME = System.currentTimeMillis();

    private boolean enabled = false;

    private final PlanFiles files;
    private final ConfigSystem configSystem;
    private final VersionChecker versionChecker;
    private final LocaleSystem localeSystem;
    private final DBSystem databaseSystem;
    private final CacheSystem cacheSystem;
    private final ListenerSystem listenerSystem;
    private final TaskSystem taskSystem;
    private final ServerInfo serverInfo;
    private final WebServerSystem webServerSystem;

    private final Processing processing;

    private final ImportSystem importSystem;
    private final ExportSystem exportSystem;
    private final DeliveryUtilities deliveryUtilities;
    private final ResolverSvc resolverService;
    private final ResourceSvc resourceService;
    private final ExtensionSvc extensionService;
    private final QuerySvc queryService;
    private final ListenerSvc listenerService;
    private final SettingsSvc settingsService;
    private final SchedulerSvc schedulerService;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    @Inject
    public PlanSystem(
            PlanFiles files,
            ConfigSystem configSystem,
            VersionChecker versionChecker,
            LocaleSystem localeSystem,
            DBSystem databaseSystem,
            CacheSystem cacheSystem,
            ListenerSystem listenerSystem,
            TaskSystem taskSystem,
            ServerInfo serverInfo,
            WebServerSystem webServerSystem,
            Processing processing,
            ImportSystem importSystem,
            ExportSystem exportSystem,
            DeliveryUtilities deliveryUtilities,
            ResolverSvc resolverService,
            ResourceSvc resourceService,
            ExtensionSvc extensionService,
            QuerySvc queryService,
            ListenerSvc listenerService,
            SettingsSvc settingsService,
            SchedulerSvc schedulerService,
            PluginLogger logger,
            ErrorLogger errorLogger,
            PlanAPI.PlanAPIHolder apiHolder
    ) {
        this.files = files;
        this.configSystem = configSystem;
        this.versionChecker = versionChecker;
        this.localeSystem = localeSystem;
        this.databaseSystem = databaseSystem;
        this.cacheSystem = cacheSystem;
        this.listenerSystem = listenerSystem;
        this.taskSystem = taskSystem;
        this.serverInfo = serverInfo;
        this.webServerSystem = webServerSystem;
        this.processing = processing;
        this.importSystem = importSystem;
        this.exportSystem = exportSystem;
        this.deliveryUtilities = deliveryUtilities;
        this.resolverService = resolverService;
        this.resourceService = resourceService;
        this.extensionService = extensionService;
        this.queryService = queryService;
        this.listenerService = listenerService;
        this.settingsService = settingsService;
        this.schedulerService = schedulerService;
        this.logger = logger;
        this.errorLogger = errorLogger;

        logger.info("§2");
        logger.info("§2           ██▌");
        logger.info("§2     ██▌   ██▌");
        logger.info("§2  ██▌██▌██▌██▌  §2Player Analytics");
        logger.info("§2  ██▌██▌██▌██▌  §fv" + versionChecker.getCurrentVersion());
        logger.info("§2");
    }

    @Deprecated
    public String getMainAddress() {
        return webServerSystem.getAddresses().getMainAddress().orElse(webServerSystem.getAddresses().getFallbackLocalhostAddress());
    }

    @Override
    public void enable() {
        extensionService.register();
        resolverService.register();
        resourceService.register();
        listenerService.register();
        settingsService.register();
        schedulerService.register();
        queryService.register();

        enableSystems(
                files,
                configSystem,
                localeSystem,
                versionChecker,
                databaseSystem,
                webServerSystem,
                processing,
                serverInfo,
                importSystem,
                exportSystem,
                cacheSystem,
                listenerSystem,
                taskSystem
        );

        // Disables Webserver if Proxy is detected in the database
        if (serverInfo.getServer().isNotProxy()) {
            processing.submitNonCritical(new NonProxyWebserverDisableChecker(
                    configSystem.getConfig(), webServerSystem.getAddresses(), webServerSystem, logger, errorLogger
            ));
        }

        extensionService.registerExtensions();
        enabled = true;

        String javaVersion = System.getProperty("java.specification.version");
        if ("1.8".equals(javaVersion) || "9".equals(javaVersion) || "10".equals(javaVersion)
        ) {
            logger.warn("! ------- Deprecation warning ------- !");
            logger.warn("Plan version 5.5 will require Java 11 or newer,");
            logger.warn("consider updating your JVM as soon as possible.");
            logger.warn("! ----------------------------------- !");
        }
    }

    private void enableSystems(SubSystem... systems) {
        for (SubSystem system : systems) {
            system.enable();
        }
    }

    @Override
    public void disable() {
        enabled = false;
        Formatters.clearSingleton();
        disableSystems(
                taskSystem,
                cacheSystem,
                listenerSystem,
                importSystem,
                exportSystem,
                processing,
                databaseSystem,
                webServerSystem,
                serverInfo,
                localeSystem,
                configSystem,
                files,
                versionChecker
        );
    }

    private void disableSystems(SubSystem... systems) {
        for (SubSystem system : systems) {
            try {
                if (system != null) {
                    system.disable();
                }
            } catch (Exception e) {
                errorLogger.warn(e, ErrorContext.builder().related("Disabling PlanSystem: " + system).build());
            }
        }
    }

    // Accessor methods.

    public VersionChecker getVersionChecker() {
        return versionChecker;
    }

    public ConfigSystem getConfigSystem() {
        return configSystem;
    }

    public PlanFiles getPlanFiles() {
        return files;
    }

    public DBSystem getDatabaseSystem() {
        return databaseSystem;
    }

    public ListenerSystem getListenerSystem() {
        return listenerSystem;
    }

    public TaskSystem getTaskSystem() {
        return taskSystem;
    }

    public WebServerSystem getWebServerSystem() {
        return webServerSystem;
    }

    public ImportSystem getImportSystem() {
        return importSystem;
    }

    public ExportSystem getExportSystem() {
        return exportSystem;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public CacheSystem getCacheSystem() {
        return cacheSystem;
    }

    public Processing getProcessing() {
        return processing;
    }

    public LocaleSystem getLocaleSystem() {
        return localeSystem;
    }

    public DeliveryUtilities getDeliveryUtilities() {
        return deliveryUtilities;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ExtensionSvc getExtensionService() {
        return extensionService;
    }

    /**
     * Originally visible for testing purposes.
     *
     * @return the error logger of the system
     * @deprecated A smell, dagger should be used to construct things instead.
     */
    @Deprecated
    public ErrorLogger getErrorLogger() {
        return errorLogger;
    }

    public static long getServerEnableTime() {
        return SERVER_ENABLE_TIME;
    }
}