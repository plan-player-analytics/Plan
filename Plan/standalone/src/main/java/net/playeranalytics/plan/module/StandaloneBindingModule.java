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
package net.playeranalytics.plan.module;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.delivery.webserver.cache.JSONFileStorage;
import com.djrapitops.plan.delivery.webserver.cache.JSONStorage;
import com.djrapitops.plan.delivery.webserver.http.JettyWebserver;
import com.djrapitops.plan.delivery.webserver.http.WebServer;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerServerInfo;
import com.djrapitops.plan.settings.ConfigSystem;
import com.djrapitops.plan.settings.ProxyConfigSystem;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import dagger.Binds;
import dagger.Module;
import net.playeranalytics.plan.PlanStandalone;
import net.playeranalytics.plan.utilities.logging.StandaloneErrorLogger;

/**
 * @author AuroraLS3
 */
@Module
public interface StandaloneBindingModule {

    @Binds
    PlanPlugin bindPlugin(PlanStandalone plugin);

    @Binds
    ServerInfo bindServerInfo(ServerServerInfo serverServerInfo);

    @Binds
    ConfigSystem bindConfigSystem(ProxyConfigSystem configSystem);

    @Binds
    JSONStorage bindJSONStorage(JSONFileStorage jsonFileStorage);

    @Binds
    WebServer bindWebserver(JettyWebserver webServer);

    @Binds
    ErrorLogger bindErrorLogger(StandaloneErrorLogger standaloneErrorLogger);
}
