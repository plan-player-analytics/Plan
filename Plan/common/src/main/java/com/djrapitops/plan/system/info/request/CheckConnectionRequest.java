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
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.ConnectionFailException;
import com.djrapitops.plan.api.exceptions.connection.GatewayException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest used for Checking Bukkit-Bungee connections.
 *
 * @author Rsl1122
 */
public class CheckConnectionRequest extends InfoRequestWithVariables {

    private final ServerInfo serverInfo;
    private final ConnectionSystem connectionSystem;

    CheckConnectionRequest(String webServerAddress, ServerInfo serverInfo, ConnectionSystem connectionSystem) {
        this.serverInfo = serverInfo;
        this.connectionSystem = connectionSystem;
        Verify.nullCheck(webServerAddress, () -> new IllegalArgumentException("webServerAddress can not be null."));

        variables.put("address", webServerAddress);
        variables.put("continue", "yes");
    }

    CheckConnectionRequest(ServerInfo serverInfo, ConnectionSystem connectionSystem) {
        this.serverInfo = serverInfo;
        this.connectionSystem = connectionSystem;
    }

    @Override
    public void runLocally() {
        /* Won't be run */
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender, address

        if (serverInfo.getServer().isProxy()) {
            attemptConnection(variables);
        }

        return DefaultResponses.SUCCESS.get();
    }

    private void attemptConnection(Map<String, String> variables) throws WebException {
        // Continue variable not present in rebound connection, leading to a single round ping.
        boolean shouldNotContinue = variables.get("continue") == null;
        if (shouldNotContinue) {
            return;
        }

        String address = variables.get("address");
        Verify.nullCheck(address, () -> new BadRequestException("WebServer Address ('address') not specified in the request."));

        UUID serverUUID = UUID.fromString(variables.get("sender"));

        Server bukkit = new Server(-1, serverUUID, "", address, -1);

        try {
            connectionSystem.sendInfoRequest(new CheckConnectionRequest(serverInfo, connectionSystem), bukkit);
        } catch (ConnectionFailException e) {
            throw new GatewayException(e.getMessage());
        }
    }
}
