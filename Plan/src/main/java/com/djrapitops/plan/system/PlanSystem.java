/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.cache.CacheSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.listeners.ListenerSystem;
import com.djrapitops.plan.system.processing.ProcessingQueue;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plan.utilities.NullCheck;
import com.djrapitops.plugin.api.Check;

/**
 * PlanSystem contains everything Plan needs to run.
 * <p>
 * This is an abstraction layer on top of Plugin instances so that tests can be run with less mocks.
 *
 * @author Rsl1122
 */
public abstract class PlanSystem implements SubSystem {

    // Initialized in this class
    protected final ProcessingQueue processingQueue;
    protected final WebServerSystem webServerSystem;
    protected final CacheSystem cacheSystem;

    // These need to be initialized in the sub class.
    protected VersionCheckSystem versionCheckSystem;
    protected FileSystem fileSystem;
    protected ConfigSystem configSystem;
    protected DBSystem databaseSystem;
    protected InfoSystem infoSystem;

    protected ListenerSystem listenerSystem;
    protected TaskSystem taskSystem;
    protected ServerInfo serverInfo;

    protected HookHandler hookHandler;
    protected PlanAPI planAPI;

    public PlanSystem() {
        processingQueue = new ProcessingQueue();
        webServerSystem = new WebServerSystem();
        cacheSystem = new CacheSystem(this);
    }

    public static PlanSystem getInstance() {
        boolean bukkitAvailable = Check.isBukkitAvailable();
        boolean bungeeAvailable = Check.isBungeeAvailable();
        if (bukkitAvailable && bungeeAvailable) {
            // TODO test system.
        } else if (bungeeAvailable) {
            return BungeeSystem.getInstance();
        } else {
            return BukkitSystem.getInstance();
        }
        throw new IllegalAccessError("PlanSystem is not available on this platform.");
    }

    @Override
    public void enable() throws EnableException {
        checkSubSystemInitialization();

        SubSystem[] systems = new SubSystem[]{
                versionCheckSystem,
                fileSystem,
                configSystem,
                databaseSystem,
                webServerSystem,
                serverInfo,
                infoSystem,
                processingQueue,
                cacheSystem,
                listenerSystem,
                taskSystem
        };
        for (SubSystem system : systems) {
            system.enable();
        }
    }

    @Override
    public void disable() {
        SubSystem[] systems = new SubSystem[]{
                cacheSystem,
                listenerSystem,
                processingQueue,
                databaseSystem,
                webServerSystem,
                infoSystem,
                serverInfo,
                taskSystem,
                configSystem,
                fileSystem,
                versionCheckSystem
        };
        for (SubSystem system : systems) {
            if (system != null) {
                system.disable();
            }
        }
    }

    private void checkSubSystemInitialization() throws EnableException {
        try {
            NullCheck.check(versionCheckSystem, new IllegalStateException("Version Check system was not initialized."));
            NullCheck.check(fileSystem, new IllegalStateException("File system was not initialized."));
            NullCheck.check(configSystem, new IllegalStateException("Config system was not initialized."));
            NullCheck.check(databaseSystem, new IllegalStateException("Database system was not initialized."));
            NullCheck.check(infoSystem, new IllegalStateException("Info system was not initialized."));
            NullCheck.check(serverInfo, new IllegalStateException("ServerInfo was not initialized."));
            NullCheck.check(listenerSystem, new IllegalStateException("Listener system was not initialized."));
            NullCheck.check(taskSystem, new IllegalStateException("Task system was not initialized."));
            NullCheck.check(hookHandler, new IllegalStateException("Plugin Hooks were not initialized."));
            NullCheck.check(planAPI, new IllegalStateException("Plan API was not initialized."));
        } catch (Exception e) {
            throw new EnableException("One of the subsystems is not initialized on enable for " + this.getClass().getSimpleName() + ".", e);
        }
    }

    // Accessor methods.

    public ProcessingQueue getProcessingQueue() {
        return processingQueue;
    }

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
}