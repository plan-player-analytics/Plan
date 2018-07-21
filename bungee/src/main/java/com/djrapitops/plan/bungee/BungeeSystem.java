/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.bungee;


import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.bungee.api.PlanBungeeAPI;
import com.djrapitops.plan.bungee.info.BungeeInfoSystem;
import com.djrapitops.plan.bungee.info.BungeeServerInfo;
import com.djrapitops.plan.bungee.listeners.BungeeListenerSystem;
import com.djrapitops.plan.bungee.tasks.BungeeTaskSystem;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.cache.BungeeCacheSystem;
import com.djrapitops.plan.system.database.BungeeDBSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.settings.PlanErrorManager;
import com.djrapitops.plan.system.settings.config.BungeeConfigSystem;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.api.utility.log.Log;

/**
 * Represents PlanSystem for PlanBungee.
 *
 * @author Rsl1122
 */
public class BungeeSystem extends PlanSystem {

    public BungeeSystem(PlanBungee plugin) {
        PlanSystem.setTestSystem(this);

        Log.setErrorManager(new PlanErrorManager());

        versionCheckSystem = new VersionCheckSystem(plugin.getVersion());
        fileSystem = new FileSystem(plugin);
        configSystem = new BungeeConfigSystem();
        databaseSystem = new BungeeDBSystem();
        cacheSystem = new BungeeCacheSystem(this);
        listenerSystem = new BungeeListenerSystem(plugin);
        taskSystem = new BungeeTaskSystem(plugin);

        infoSystem = new BungeeInfoSystem();
        serverInfo = new BungeeServerInfo(plugin);

        hookHandler = new HookHandler();
        planAPI = new PlanBungeeAPI(this);
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
