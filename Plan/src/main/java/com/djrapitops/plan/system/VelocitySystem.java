/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.api.VelocityAPI;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.cache.ProxyCacheSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.ProxyDBSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.ProxyInfoSystem;
import com.djrapitops.plan.system.info.server.VelocityServerInfo;
import com.djrapitops.plan.system.listeners.VelocityListenerSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.PlanErrorManager;
import com.djrapitops.plan.system.settings.config.BungeeConfigSystem;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plan.system.tasks.VelocityTaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.function.Supplier;

/**
 * Represents PlanSystem for PlanVelocity.
 * 
 * Based on BungeeSystem
 *
 * @author MicleBrick
 */
public class VelocitySystem extends PlanSystem {

    public VelocitySystem(PlanVelocity plugin) {
        setTestSystem(this);

        Log.setErrorManager(new PlanErrorManager());

        Supplier<Locale> localeSupplier = () -> getLocaleSystem().getLocale();

        versionCheckSystem = new VersionCheckSystem(plugin.getVersion(), localeSupplier);
        fileSystem = new FileSystem(plugin);
        configSystem = new BungeeConfigSystem(); // not sure if this needs to be different for velocity
        databaseSystem = new ProxyDBSystem(localeSupplier);
        cacheSystem = new ProxyCacheSystem(this);
        listenerSystem = new VelocityListenerSystem(plugin);
        taskSystem = new VelocityTaskSystem(plugin);

        infoSystem = new ProxyInfoSystem();
        serverInfo = new VelocityServerInfo(plugin);

        hookHandler = new HookHandler();
        planAPI = new VelocityAPI(this);
    }

    public static VelocitySystem getInstance() {
        return PlanVelocity.getInstance().getSystem();
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
