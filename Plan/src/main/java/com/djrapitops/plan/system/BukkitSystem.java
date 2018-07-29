/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.ShutdownHook;
import com.djrapitops.plan.api.ServerAPI;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.ServerDBSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.ServerInfoSystem;
import com.djrapitops.plan.system.info.server.BukkitServerInfo;
import com.djrapitops.plan.system.listeners.BukkitListenerSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.PlanErrorManager;
import com.djrapitops.plan.system.settings.config.ServerConfigSystem;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plan.system.tasks.BukkitTaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.function.Supplier;

/**
 * Represents PlanSystem for Plan.
 *
 * @author Rsl1122
 */
public class BukkitSystem extends PlanSystem implements ServerSystem {

    public BukkitSystem(Plan plugin) {
        setTestSystem(this);

        Log.setErrorManager(new PlanErrorManager());

        Supplier<Locale> localeSupplier = () -> getLocaleSystem().getLocale();

        versionCheckSystem = new VersionCheckSystem(plugin.getVersion(), localeSupplier);
        fileSystem = new FileSystem(plugin);
        configSystem = new ServerConfigSystem();
        databaseSystem = new ServerDBSystem(localeSupplier);
        listenerSystem = new BukkitListenerSystem(plugin);
        taskSystem = new BukkitTaskSystem(plugin);

        infoSystem = new ServerInfoSystem(localeSupplier);
        serverInfo = new BukkitServerInfo(plugin);

        hookHandler = new HookHandler();
        planAPI = new ServerAPI(this);

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
