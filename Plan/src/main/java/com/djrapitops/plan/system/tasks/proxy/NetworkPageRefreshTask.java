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
package com.djrapitops.plan.system.tasks.proxy;

import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NetworkPageRefreshTask extends AbsRunnable {

    private final ServerInfo serverInfo;

    @Inject
    public NetworkPageRefreshTask(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public void run() {
        ResponseCache.clearResponse(PageId.SERVER.of(serverInfo.getServerUUID()));
    }
}
