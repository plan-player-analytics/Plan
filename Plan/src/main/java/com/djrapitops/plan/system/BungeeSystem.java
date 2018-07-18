/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.api.BungeeAPI;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.cache.BungeeCacheSystem;
import com.djrapitops.plan.system.database.BungeeDBSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.BungeeInfoSystem;
import com.djrapitops.plan.system.info.server.BungeeServerInfo;
import com.djrapitops.plan.system.listeners.BungeeListenerSystem;
import com.djrapitops.plan.system.settings.PlanErrorManager;
import com.djrapitops.plan.system.settings.config.BungeeConfigSystem;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plan.system.tasks.BungeeTaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.api.utility.log.Log;

/**
 * Represents PlanSystem for PlanBungee.
 *
 * @author Rsl1122
 */
public class BungeeSystem extends PlanSystem {

    public BungeeSystem(PlanBungee plugin) {
        setTestSystem(this);

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
        planAPI = new BungeeAPI(this);
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
