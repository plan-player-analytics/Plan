/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.ShutdownHook;
import com.djrapitops.plan.api.ServerAPI;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.BukkitDBSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.BukkitInfoSystem;
import com.djrapitops.plan.system.info.server.SpongeServerInfo;
import com.djrapitops.plan.system.listeners.SpongeListenerSystem;
import com.djrapitops.plan.system.settings.PlanErrorManager;
import com.djrapitops.plan.system.settings.config.BukkitConfigSystem;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plan.system.tasks.SpongeTaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.utility.log.Log;

/**
 * Represents PlanSystem for PlanSponge.
 *
 * @author Rsl1122
 */
public class SpongeSystem extends PlanSystem implements ServerSystem {

    public SpongeSystem(PlanSponge plugin) {
        testSystem = this;

        Log.setErrorManager(new PlanErrorManager());

        versionCheckSystem = new VersionCheckSystem(plugin.getVersion());
        fileSystem = new FileSystem(plugin);
        configSystem = new BukkitConfigSystem();
        databaseSystem = new BukkitDBSystem();
        listenerSystem = new SpongeListenerSystem(plugin);
        taskSystem = new SpongeTaskSystem(plugin);

        infoSystem = new BukkitInfoSystem();
        serverInfo = new SpongeServerInfo();

        hookHandler = new HookHandler();
        planAPI = new ServerAPI(this);

        StaticHolder.saveInstance(ShutdownHook.class, plugin.getClass());
        new ShutdownHook().register();
    }

    public static SpongeSystem getInstance() {
        return PlanSponge.getInstance().getSystem();
    }

    @Override
    public void enable() throws EnableException {
        super.enable();
        NetworkSettings.loadSettingsFromDB();
    }
}