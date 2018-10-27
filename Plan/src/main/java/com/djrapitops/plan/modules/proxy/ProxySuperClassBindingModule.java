package com.djrapitops.plan.modules.proxy;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.api.ProxyAPI;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.cache.ProxyDataCache;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.ProxyDBSystem;
import com.djrapitops.plan.system.importing.EmptyImportSystem;
import com.djrapitops.plan.system.importing.ImportSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.ProxyInfoSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.connection.ProxyConnectionSystem;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.config.ProxyConfigSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for binding proxy server classes to super classes.
 *
 * @author Rsl1122
 */
@Module
public class ProxySuperClassBindingModule {

    @Provides
    @Singleton
    PlanAPI provideProxyPlanAPI(ProxyAPI proxyAPI) {
        return proxyAPI;
    }

    @Provides
    @Singleton
    DBSystem provideProxyDatabaseSystem(ProxyDBSystem proxyDBSystem) {
        return proxyDBSystem;
    }

    @Provides
    @Singleton
    ConfigSystem provideProxyConfigSystem(ProxyConfigSystem proxyConfigSystem) {
        return proxyConfigSystem;
    }

    @Provides
    @Singleton
    InfoSystem provideProxyInfoSystem(ProxyInfoSystem proxyInfoSystem) {
        return proxyInfoSystem;
    }

    @Provides
    @Singleton
    ConnectionSystem provideProxyConnectionSystem(ProxyConnectionSystem proxyConnectionSystem) {
        return proxyConnectionSystem;
    }

    @Provides
    @Singleton
    DataCache provideProxyDataCache(ProxyDataCache proxyDataCache) {
        return proxyDataCache;
    }

    @Provides
    @Singleton
    ImportSystem provideImportSystem() {
        return new EmptyImportSystem();
    }

}