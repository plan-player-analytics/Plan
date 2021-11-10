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
package com.djrapitops.plan.delivery.domain.datatransfer;

import com.djrapitops.plan.identification.Server;

/**
 * Represents outgoing server information json.
 */
public class ServerDto {

    private final String serverUUID;
    private final String serverName;
    private final boolean proxy;

    public ServerDto(String serverUUID, String serverName, boolean proxy) {
        this.serverUUID = serverUUID;
        this.serverName = serverName;
        this.proxy = proxy;
    }

    public static ServerDto fromServer(Server server) {
        return new ServerDto(server.getUuid().toString(), server.getIdentifiableName(), server.isProxy());
    }

    public String getServerUUID() {
        return serverUUID;
    }

    public String getServerName() {
        return serverName;
    }

    public boolean isProxy() {
        return proxy;
    }

    @Override
    public String toString() {
        return "ServerDto{" +
                "serverUUID='" + serverUUID + '\'' +
                ", serverName='" + serverName + '\'' +
                ", proxy=" + proxy +
                '}';
    }
}
