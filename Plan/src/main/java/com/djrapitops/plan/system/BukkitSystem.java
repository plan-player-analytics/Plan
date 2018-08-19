/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.ShutdownHook;
import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.export.ExportSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.BukkitServerInfo;
import com.djrapitops.plan.system.listeners.BukkitListenerSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.PlanErrorManager;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plan.system.tasks.BukkitTaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.utility.log.Log;

import javax.inject.Inject;
import java.util.function.Supplier;

/**
 * Represents PlanSystem for Plan.
 *
 * @author Rsl1122
 */
public class BukkitSystem extends PlanSystem implements ServerSystem {

    @Inject
    public BukkitSystem(Plan plugin,
                        VersionCheckSystem versionCheckSystem,
                        FileSystem fileSystem,
                        ConfigSystem serverConfigSystem,
                        InfoSystem serverInfoSystem,
                        HookHandler hookHandler,
                        PlanAPI planAPI,
                        ExportSystem exportSystem,
                        DBSystem serverDBSystem
    ) {
        setTestSystem(this);

        Log.setErrorManager(new PlanErrorManager());

        Supplier<Locale> localeSupplier = () -> getLocaleSystem().getLocale();

        this.versionCheckSystem = versionCheckSystem;
        this.fileSystem = fileSystem;
        this.configSystem = serverConfigSystem;
        this.exportSystem = exportSystem;
        this.databaseSystem = serverDBSystem;
        listenerSystem = new BukkitListenerSystem(plugin);
        taskSystem = new BukkitTaskSystem(plugin);

        infoSystem = serverInfoSystem;
        serverInfo = new BukkitServerInfo(plugin);

        this.hookHandler = hookHandler;
        this.planAPI = planAPI;

        StaticHolder.saveInstance(ShutdownHook.class, plugin.getClass());
        new ShutdownHook().register();
    }

    public static BukkitSystem getInstance() {
        return Plan.getInstance().getSystem();
    }

    @Override
    public void enable() throws EnableException {
        super.enable();
        NetworkSettings.loadSettingsFromDB();
    }
}