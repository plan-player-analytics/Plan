/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.bukkit;

import com.djrapitops.plan.ShutdownHook;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.bukkit.api.PlanBukkitAPI;
import com.djrapitops.plan.bukkit.database.BukkitDatabaseSystem;
import com.djrapitops.plan.bukkit.info.BukkitServerInfo;
import com.djrapitops.plan.bukkit.listeners.BukkitListenerSystem;
import com.djrapitops.plan.bukkit.tasks.BukkitTaskSystem;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.ServerSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.ServerInfoSystem;
import com.djrapitops.plan.system.settings.PlanErrorManager;
import com.djrapitops.plan.system.settings.config.ServerConfigSystem;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.utility.log.Log;

/**
 * Represents PlanSystem for PlanBukkit.
 *
 * @author Rsl1122
 */
public class BukkitSystem extends PlanSystem implements ServerSystem {

    public BukkitSystem(PlanBukkit plugin) {
        PlanSystem.setTestSystem(this);

        Log.setErrorManager(new PlanErrorManager());

        versionCheckSystem = new VersionCheckSystem(plugin.getVersion());
        fileSystem = new FileSystem(plugin);
        configSystem = new ServerConfigSystem();
        databaseSystem = new BukkitDatabaseSystem();
        listenerSystem = new BukkitListenerSystem(plugin);
        taskSystem = new BukkitTaskSystem(plugin);

        infoSystem = new ServerInfoSystem();
        serverInfo = new BukkitServerInfo(plugin);

        hookHandler = new HookHandler();
        planAPI = new PlanBukkitAPI(this);

        StaticHolder.saveInstance(ShutdownHook.class, plugin.getClass());
        new ShutdownHook().register();
    }

    @Override
    public void enable() throws EnableException {
        super.enable();
        NetworkSettings.loadSettingsFromDB();
    }
}
