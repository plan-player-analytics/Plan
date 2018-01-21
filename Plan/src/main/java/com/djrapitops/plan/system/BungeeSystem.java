/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.database.BungeeDBSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.BukkitInfoSystem;
import com.djrapitops.plan.system.info.server.BungeeServerInfo;
import com.djrapitops.plan.system.listeners.BungeeListenerSystem;
import com.djrapitops.plan.system.settings.config.BungeeConfigSystem;
import com.djrapitops.plan.system.tasks.BungeeTaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;

/**
 * Represents PlanSystem for PlanBungee.
 *
 * @author Rsl1122
 */
public class BungeeSystem extends PlanSystem {

    public BungeeSystem(PlanBungee plugin) {
        versionCheckSystem = new VersionCheckSystem(plugin.getVersion());
        fileSystem = new FileSystem(plugin);
        configSystem = new BungeeConfigSystem();
        databaseSystem = new BungeeDBSystem();
        listenerSystem = new BungeeListenerSystem(plugin);
        taskSystem = new BungeeTaskSystem(plugin);

        infoSystem = new BukkitInfoSystem();
        serverInfo = new BungeeServerInfo(plugin);
    }

    public static BungeeSystem getInstance() {
        return PlanBungee.getInstance().getSystem();
    }
}