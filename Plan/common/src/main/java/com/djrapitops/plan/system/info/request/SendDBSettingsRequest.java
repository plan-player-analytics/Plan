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
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.errors.BadRequestResponse;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.utilities.Verify;

import java.net.SocketException;
import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest used for requesting DB settings from Bungee.
 *
 * @author Rsl1122
 */
public class SendDBSettingsRequest extends InfoRequestWithVariables implements SetupRequest {

    private final InfoRequestFactory infoRequestFactory;
    private final ConnectionSystem connectionSystem;

    SendDBSettingsRequest(
            InfoRequestFactory infoRequestFactory, ConnectionSystem connectionSystem
    ) {
        this.infoRequestFactory = infoRequestFactory;
        this.connectionSystem = connectionSystem;
    }

    SendDBSettingsRequest(
            String webServerAddress,
            InfoRequestFactory infoRequestFactory, ConnectionSystem connectionSystem
    ) {
        this.infoRequestFactory = infoRequestFactory;
        this.connectionSystem = connectionSystem;

        Verify.nullCheck(webServerAddress, () -> new IllegalArgumentException("webServerAddress can not be null."));
        variables.put("address", webServerAddress);
    }

    @Override
    public void runLocally() {
        /* Won't be run */
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender, address
        if (Check.isBukkitAvailable()) {
            return new BadRequestResponse("Not supposed to be called on a Bukkit server");
        }
        if (Check.isSpongeAvailable()) {
            return new BadRequestResponse("Not supposed to be called on a Sponge server");
        }

        String address = variables.get("address");
        Verify.nullCheck(address, () -> new BadRequestException("WebServer Address ('address') not specified in the request."));

        UUID serverUUID = UUID.fromString(variables.get("sender"));

        Server bukkit = new Server(-1, serverUUID, null, address, -1);

        try {
            connectionSystem.sendInfoRequest(infoRequestFactory.saveDBSettingsRequest(), bukkit);
        } catch (ConnectionFailException e) {
            Throwable cause = e.getCause();
            if (!(cause instanceof SocketException) || !cause.getMessage().contains("Unexpected end of file from server")) {
                throw new GatewayException(e.getMessage());
            }
        }

        return DefaultResponses.SUCCESS.get();
    }
}
