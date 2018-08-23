/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.ShutdownHook;
import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.export.ExportSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.SpongeServerInfo;
import com.djrapitops.plan.system.listeners.SpongeListenerSystem;
import com.djrapitops.plan.system.locale.LocaleSystem;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plan.system.tasks.SpongeTaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.webserver.WebServerSystem;

import javax.inject.Inject;

/**
 * Represents PlanSystem for PlanSponge.
 *
 * @author Rsl1122
 */
public class SpongeSystem extends PlanSystem implements ServerSystem {

    @Inject
    public SpongeSystem(PlanSponge plugin,
                        VersionCheckSystem versionCheckSystem,
                        FileSystem fileSystem,
                        ConfigSystem serverConfigSystem,
                        LocaleSystem localeSystem,
                        InfoSystem serverInfoSystem,
                        SpongeServerInfo serverInfo,
                        DBSystem databaseSystem,
                        WebServerSystem webServerSystem,
                        SpongeListenerSystem spongeListenerSystem,
                        SpongeTaskSystem spongeTaskSystem,
                        ExportSystem exportSystem,
                        HookHandler hookHandler,
                        PlanAPI planAPI,
                        ShutdownHook shutdownHook
    ) {
        super(webServerSystem, localeSystem);
        setTestSystem(this);

        this.versionCheckSystem = versionCheckSystem;
        this.fileSystem = fileSystem;
        this.configSystem = serverConfigSystem;
        this.exportSystem = exportSystem;
        this.databaseSystem = databaseSystem;
        listenerSystem = spongeListenerSystem;
        taskSystem = spongeTaskSystem;

        infoSystem = serverInfoSystem;
        this.serverInfo = serverInfo;

        this.hookHandler = hookHandler;
        this.planAPI = planAPI;

        shutdownHook.register();
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
