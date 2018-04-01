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
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

/**
 * PlanSystem contains everything Plan needs to run.
 * <p>
 * This is an abstraction layer on top of Plugin instances so that tests can be run with less mocks.
 *
 * @author Rsl1122
 */
public abstract class PlanSystem implements SubSystem {

    protected static PlanSystem testSystem;

    // Initialized in this class
    private Processing processing;
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

    // Not a SubSystem.
    protected PlanAPI planAPI;

    public PlanSystem() {
        processing = new Processing();
        webServerSystem = new WebServerSystem();
        cacheSystem = new CacheSystem(this);
    }

    public static PlanSystem getInstance() {
        boolean bukkitAvailable = Check.isBukkitAvailable();
        boolean bungeeAvailable = Check.isBungeeAvailable();
        if (bukkitAvailable && bungeeAvailable) {
            return testSystem;
        } else if (bungeeAvailable) {
            return BungeeSystem.getInstance();
        } else if (bukkitAvailable) {
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
                processing,
                serverInfo,
                infoSystem,
                cacheSystem,
                listenerSystem,
                taskSystem,
                hookHandler
        };
        for (SubSystem system : systems) {
            system.enable();
        }
    }

    @Override
    public void disable() {
        SubSystem[] systems = new SubSystem[]{
                taskSystem,
                hookHandler,
                cacheSystem,
                listenerSystem,
                processing,
                databaseSystem,
                webServerSystem,
                infoSystem,
                serverInfo,
                configSystem,
                fileSystem,
                versionCheckSystem
        };
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

    private void checkSubSystemInitialization() throws EnableException {
        try {
            Verify.nullCheck(versionCheckSystem, () -> new IllegalStateException("Version Check system was not initialized."));
            Verify.nullCheck(fileSystem, () -> new IllegalStateException("File system was not initialized."));
            Verify.nullCheck(configSystem, () -> new IllegalStateException("Config system was not initialized."));
            Verify.nullCheck(databaseSystem, () -> new IllegalStateException("Database system was not initialized."));
            Verify.nullCheck(infoSystem, () -> new IllegalStateException("Info system was not initialized."));
            Verify.nullCheck(serverInfo, () -> new IllegalStateException("ServerInfo was not initialized."));
            Verify.nullCheck(listenerSystem, () -> new IllegalStateException("Listener system was not initialized."));
            Verify.nullCheck(taskSystem, () -> new IllegalStateException("Task system was not initialized."));
            Verify.nullCheck(hookHandler, () -> new IllegalStateException("Plugin Hooks were not initialized."));
            Verify.nullCheck(planAPI, () -> new IllegalStateException("Plan API was not initialized."));
        } catch (Exception e) {
            throw new EnableException("One of the subsystems is not initialized on enable for " + this.getClass().getSimpleName() + ": " + e.getMessage());
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
}