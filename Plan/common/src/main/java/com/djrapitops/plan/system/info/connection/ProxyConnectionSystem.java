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
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.WebServer;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * ConnectionSystem for proxy servers.
 *
 * @author Rsl1122
 */
@Singleton
public class ProxyConnectionSystem extends ConnectionSystem {

    private final Lazy<WebServer> webServer;

    @Inject
    public ProxyConnectionSystem(
            Lazy<WebServer> webServer,
            ServerInfo serverInfo
    ) {
        super(serverInfo);
        this.webServer = webServer;
    }

    @Override
    public boolean isServerAvailable() {
        return true;
    }

    @Override
    public String getMainAddress() {
        return webServer.get().getAccessAddress();
    }

}
