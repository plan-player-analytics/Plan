/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.cache.CacheSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.export.ExportSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.listeners.ListenerSystem;
import com.djrapitops.plan.system.locale.LocaleSystem;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.utility.log.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * PlanSystem contains everything Plan needs to run.
 * <p>
 * This is an abstraction layer on top of Plugin instances so that tests can be run with less mocks.
 *
 * @author Rsl1122
 */
@Singleton
public class PlanSystem implements SubSystem {

    private final FileSystem fileSystem;
    private final ConfigSystem configSystem;
    private final VersionCheckSystem versionCheckSystem;
    private final LocaleSystem localeSystem;
    private final DBSystem databaseSystem;
    private final CacheSystem cacheSystem;
    private final ListenerSystem listenerSystem;
    private final TaskSystem taskSystem;
    private final InfoSystem infoSystem;
    private final ServerInfo serverInfo;
    private final WebServerSystem webServerSystem;

    private final Processing processing;

    private final ExportSystem exportSystem;
    private final HookHandler hookHandler;
    private final PlanAPI planAPI;

    @Inject
    public PlanSystem(
            FileSystem fileSystem,
            ConfigSystem configSystem,
            VersionCheckSystem versionCheckSystem,
            LocaleSystem localeSystem,
            DBSystem databaseSystem,
            CacheSystem cacheSystem,
            ListenerSystem listenerSystem,
            TaskSystem taskSystem,
            InfoSystem infoSystem,
            ServerInfo serverInfo,
            WebServerSystem webServerSystem,
            //Processing processing,
            ExportSystem exportSystem,
            HookHandler hookHandler,
            PlanAPI planAPI
    ) {
        this.fileSystem = fileSystem;
        this.configSystem = configSystem;
        this.versionCheckSystem = versionCheckSystem;
        this.localeSystem = localeSystem;
        this.databaseSystem = databaseSystem;
        this.cacheSystem = cacheSystem;
        this.listenerSystem = listenerSystem;
        this.taskSystem = taskSystem;
        this.infoSystem = infoSystem;
        this.serverInfo = serverInfo;
        this.webServerSystem = webServerSystem;
        this.processing = new Processing(localeSystem::getLocale);
        this.exportSystem = exportSystem;
        this.hookHandler = hookHandler;
        this.planAPI = planAPI;
    }

    @Deprecated
    public static PlanSystem getInstance() {
        return PlanPlugin.getInstance().getSystem();
    }

    @Override
    public void enable() throws EnableException {
        enableSystems(
                fileSystem,
                configSystem,
                localeSystem,
                versionCheckSystem,
                databaseSystem,
                exportSystem,
                webServerSystem,
                processing,
                serverInfo,
                infoSystem,
                cacheSystem,
                listenerSystem,
                taskSystem,
                hookHandler
        );
    }

    private void enableSystems(SubSystem... systems) throws EnableException {
        for (SubSystem system : systems) {
            system.enable();
        }
    }

    @Override
    public void disable() {
        disableSystems(
                taskSystem,
                hookHandler,
                cacheSystem,
                listenerSystem,
                exportSystem,
                processing,
                databaseSystem,
                webServerSystem,
                infoSystem,
                serverInfo,
                localeSystem,
                configSystem,
                fileSystem,
                versionCheckSystem
        );
    }

    private void disableSystems(SubSystem... systems) {
        for (SubSystem system : systems) {
            try {
                if (system != null) {
                    system.disable();
                }
            } catch (Exception e) {
                Log.toLog(this.getClass(), e);
            }
        }
    }

    // Accessor methods.

    public VersionCheckSystem getVersionCheckSystem() {
        return versionCheckSystem;
    }

    public ConfigSystem getConfigSystem() {
        return configSystem;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
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

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public CacheSystem getCacheSystem() {
        return cacheSystem;
    }

    public InfoSystem getInfoSystem() {
        return infoSystem;
    }

    public HookHandler getHookHandler() {
        return hookHandler;
    }

    public PlanAPI getPlanAPI() {
        return planAPI;
    }

    public Processing getProcessing() {
        return processing;
    }

    public LocaleSystem getLocaleSystem() {
        return localeSystem;
    }
}