/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
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