/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
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
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest used for Checking Bukkit-Bungee connections.
 *
 * @author Rsl1122
 */
public class CheckConnectionRequest extends InfoRequestWithVariables {

    private final ConnectionSystem connectionSystem;

    CheckConnectionRequest(String webServerAddress, ConnectionSystem connectionSystem) {
        this.connectionSystem = connectionSystem;
        Verify.nullCheck(webServerAddress, () -> new IllegalArgumentException("webServerAddress can not be null."));

        variables.put("address", webServerAddress);
        variables.put("continue", "yes");
    }

    CheckConnectionRequest(ConnectionSystem connectionSystem) {
        this.connectionSystem = connectionSystem;
    }

    @Override
    public void runLocally() {
        /* Won't be run */
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender, address

        if (Check.isBungeeAvailable()) {
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
            connectionSystem.sendInfoRequest(new CheckConnectionRequest(connectionSystem), bukkit);
        } catch (ConnectionFailException e) {
            throw new GatewayException(e.getMessage());
        }
    }
}
