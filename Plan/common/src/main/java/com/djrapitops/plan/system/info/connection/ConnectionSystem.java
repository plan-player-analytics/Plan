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

import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.system.info.server.ServerInfo;

/**
 * ConnectionSystem manages proxy server status.
 *
 * @author Rsl1122
 * @deprecated Usage should be replaced with Database query {@link ServerQueries#fetchProxyServerInformation()}
 */
@Deprecated
public abstract class ConnectionSystem {

    protected final ServerInfo serverInfo;

    public ConnectionSystem(
            ServerInfo serverInfo
    ) {
        this.serverInfo = serverInfo;
    }

    @Deprecated
    public abstract boolean isServerAvailable();

    @Deprecated
    public abstract String getMainAddress();
}
