/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.ShutdownHook;
import com.djrapitops.plan.api.BukkitAPI;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.BukkitDBSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.BukkitInfoSystem;
import com.djrapitops.plan.system.info.server.BukkitServerInfo;
import com.djrapitops.plan.system.listeners.BukkitListenerSystem;
import com.djrapitops.plan.system.settings.config.BukkitConfigSystem;
import com.djrapitops.plan.system.tasks.BukkitTaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.StaticHolder;

/**
 * Represents PlanSystem for Plan.
 *
 * @author Rsl1122
 */
public class BukkitSystem extends PlanSystem {

    public BukkitSystem(Plan plugin) {
        versionCheckSystem = new VersionCheckSystem(plugin.getVersion());
        fileSystem = new FileSystem(plugin);
        configSystem = new BukkitConfigSystem();
        databaseSystem = new BukkitDBSystem();
        listenerSystem = new BukkitListenerSystem(plugin);
        taskSystem = new BukkitTaskSystem(plugin);

        infoSystem = new BukkitInfoSystem();
        serverInfo = new BukkitServerInfo(plugin);

        hookHandler = new HookHandler();
        planAPI = new BukkitAPI(this);

        StaticHolder.saveInstance(ShutdownHook.class, plugin.getClass());
        new ShutdownHook().register();
    }

    public static BukkitSystem getInstance() {
        return Plan.getInstance().getSystem();
    }
}