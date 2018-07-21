/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.sponge;

import com.djrapitops.plan.ShutdownHook;
import com.djrapitops.plan.api.CommonAPI;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.sponge.database.SpongeDatabaseSystem;
import com.djrapitops.plan.sponge.info.SpongeServerInfo;
import com.djrapitops.plan.sponge.listeners.SpongeListenerSystem;
import com.djrapitops.plan.sponge.tasks.SpongeTaskSystem;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.ServerSystem;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.ServerInfoSystem;
import com.djrapitops.plan.system.settings.PlanErrorManager;
import com.djrapitops.plan.system.settings.config.SpongeConfigSystem;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.UUID;

/**
 * Represents PlanSystem for PlanSponge.
 *
 * @author Rsl1122
 */
public class SpongeSystem extends PlanSystem implements ServerSystem {

    public SpongeSystem(PlanSponge plugin) {
        PlanSystem.setTestSystem(this);

        Log.setErrorManager(new PlanErrorManager());

        versionCheckSystem = new VersionCheckSystem(plugin.getVersion());
        fileSystem = new FileSystem(plugin);
        configSystem = new SpongeConfigSystem();
        databaseSystem = new SpongeDatabaseSystem();
        listenerSystem = new SpongeListenerSystem(plugin);
        taskSystem = new SpongeTaskSystem(plugin);

        infoSystem = new ServerInfoSystem();
        serverInfo = new SpongeServerInfo();

        hookHandler = new HookHandler();
        planAPI = new CommonAPI() {
            @Override
            public void addPluginDataSource(PluginData pluginData) {
                getHookHandler().addPluginDataSource(pluginData);
            }

            @Override
            public String getPlayerName(UUID uuid) {
                return getCacheSystem().getDataCache().getName(uuid);
            }

            @Override
            public FetchOperations fetchFromPlanDB() {
                return getDatabaseSystem().getActiveDatabase().fetch();
            }
        };

        StaticHolder.saveInstance(ShutdownHook.class, plugin.getClass());
        new ShutdownHook().register();
    }

    @Override
    public void enable() throws EnableException {
        super.enable();
        NetworkSettings.loadSettingsFromDB();
    }
}
