/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.modules;

import com.djrapitops.plan.api.CommonAPI;
import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.ProxyDBSystem;
import com.djrapitops.plan.system.importing.EmptyImportSystem;
import com.djrapitops.plan.system.importing.ImportSystem;
import com.djrapitops.plan.system.settings.ConfigSystem;
import com.djrapitops.plan.system.settings.ProxyConfigSystem;
import dagger.Binds;
import dagger.Module;

/**
 * Dagger module for binding proxy server classes to super classes.
 *
 * @author Rsl1122
 */
@Module
public interface ProxySuperClassBindingModule {

    @Binds
    PlanAPI bindProxyPlanAPI(CommonAPI proxyAPI);

    @Binds
    DBSystem bindProxyDatabaseSystem(ProxyDBSystem proxyDBSystem);

    @Binds
    ConfigSystem bindProxyConfigSystem(ProxyConfigSystem proxyConfigSystem);

    @Binds
    ImportSystem bindImportSystem(EmptyImportSystem emptyImportSystem);

}