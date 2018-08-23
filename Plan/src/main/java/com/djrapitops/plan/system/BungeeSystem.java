/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.cache.BungeeCacheSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.export.ExportSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.listeners.BungeeListenerSystem;
import com.djrapitops.plan.system.locale.LocaleSystem;
import com.djrapitops.plan.system.settings.config.BungeeConfigSystem;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plan.system.tasks.BungeeTaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.webserver.WebServerSystem;

import javax.inject.Inject;

/**
 * Represents PlanSystem for PlanBungee.
 *
 * @author Rsl1122
 */
public class BungeeSystem extends PlanSystem {

    @Inject
    public BungeeSystem(
            VersionCheckSystem versionCheckSystem,
            FileSystem fileSystem,
            BungeeConfigSystem bungeeConfigSystem,
            LocaleSystem localeSystem,
            InfoSystem infoSystem,
            ServerInfo serverInfo,
            BungeeCacheSystem bungeeCacheSystem,
            DBSystem databaseSystem,
            WebServerSystem webServerSystem,
            BungeeListenerSystem bungeeListenerSystem,
            BungeeTaskSystem bungeeTaskSystem,
            HookHandler hookHandler,
            ExportSystem exportSystem,
            PlanAPI planAPI
    ) {
        super(webServerSystem, localeSystem);
        setTestSystem(this);

        this.versionCheckSystem = versionCheckSystem;
        this.fileSystem = fileSystem;
        configSystem = bungeeConfigSystem;
        this.exportSystem = exportSystem;
        this.databaseSystem = databaseSystem;
        cacheSystem = bungeeCacheSystem;
        listenerSystem = bungeeListenerSystem;
        taskSystem = bungeeTaskSystem;

        this.infoSystem = infoSystem;
        this.serverInfo = serverInfo;

        this.hookHandler = hookHandler;
        this.planAPI = planAPI;
    }

    public static BungeeSystem getInstance() {
        return PlanBungee.getInstance().getSystem();
    }

    public void setDatabaseSystem(DBSystem dbSystem) {
        this.databaseSystem = dbSystem;
    }

    @Override
    public void enable() throws EnableException {
        super.enable();
        NetworkSettings.placeSettingsToDB();
    }
}
