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
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.errors.BadRequestResponse;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.utilities.Verify;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest used for requesting DB settings from Bungee.
 *
 * @author Rsl1122
 */
public class SendDBSettingsRequest extends InfoRequestWithVariables implements SetupRequest {

    private final PlanConfig config;
    private final InfoRequestFactory infoRequestFactory;
    private final ConnectionSystem connectionSystem;

    SendDBSettingsRequest(
            PlanConfig config,
            InfoRequestFactory infoRequestFactory, ConnectionSystem connectionSystem
    ) {
        this.config = config;
        this.infoRequestFactory = infoRequestFactory;
        this.connectionSystem = connectionSystem;
    }

    SendDBSettingsRequest(
            String webServerAddress,
            PlanConfig config,
            InfoRequestFactory infoRequestFactory, ConnectionSystem connectionSystem
    ) {
        this.config = config;
        this.infoRequestFactory = infoRequestFactory;
        this.connectionSystem = connectionSystem;

        Verify.nullCheck(webServerAddress, () -> new IllegalArgumentException("webServerAddress can not be null."));
        variables.put("address", webServerAddress);
        variables.put("WebServerPort", config.getString(Settings.WEBSERVER_PORT));
        variables.put("ServerName", config.getString(Settings.SERVER_NAME).replaceAll("[^a-zA-Z0-9_\\s]", "_"));
        variables.put("ThemeBase", config.getString(Settings.THEME_BASE));
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

        String webServerPortS = variables.get("WebServerPort");
        String serverName = variables.get("ServerName");
        String themeBase = variables.get("ThemeBase");
        Verify.nullCheck(webServerPortS, () -> new BadRequestException("WebServer Port ('WebServerPort') not specified in the request."));
        Verify.nullCheck(serverName, () -> new BadRequestException("Server Name ('ServerName') not specified in the request."));
        Verify.nullCheck(themeBase, () -> new BadRequestException("Theme Base ('ThemeBase') not specified in the request."));

        UUID serverUUID = UUID.fromString(variables.get("sender"));
        setOriginalSettings(serverUUID, webServerPortS, serverName, themeBase);

        Server bukkit = new Server(-1, serverUUID, serverName, address, -1);

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

    private void setOriginalSettings(UUID serverUUID, String webServerPortS, String serverName, String themeBase) throws InternalErrorException {
        Map<String, Object> settings = new HashMap<>();
        int webServerPort = Integer.parseInt(webServerPortS);
        settings.put("WebServerPort", webServerPort);
        settings.put("ServerName", serverName);
        settings.put("ThemeBase", themeBase);

        try {
            config.getNetworkSettings().getServerSpecificSettings().addOriginalBukkitSettings(serverUUID, settings);
        } catch (IOException e) {
            throw new InternalErrorException("Failed to add Bukkit settings to config", e);
        }
    }
}
