/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.api.BadRequestResponse;
import com.djrapitops.plan.utilities.NullCheck;
import com.djrapitops.plugin.api.Check;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest used for requesting DB settings from Bungee.
 *
 * @author Rsl1122
 */
public class SendDBSettingsRequest extends InfoRequestWithVariables implements SetupRequest {

    public SendDBSettingsRequest(String webServerAddress) {
        NullCheck.check(webServerAddress, new IllegalArgumentException("webServerAddress can not be null."));

        variables.put("address", webServerAddress);
        variables.put("WebServerPort", Integer.toString(Settings.WEBSERVER_PORT.getNumber()));
        variables.put("ServerName", Settings.SERVER_NAME.toString().replaceAll("[^a-zA-Z0-9_\\s]", "_"));
        variables.put("ThemeBase", Settings.THEME_BASE.toString());
    }

    private SendDBSettingsRequest() {
    }

    public static SendDBSettingsRequest createHandler() {
        return new SendDBSettingsRequest();
    }

    @Override
    public void placeDataToDatabase() {
        /* Not necessary */
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

        String address = variables.get("address");
        NullCheck.check(address, new BadRequestException("WebServer Address ('address') not specified in the request."));

        String webServerPortS = variables.get("WebServerPort");
        String serverName = variables.get("ServerName");
        String themeBase = variables.get("ThemeBase");
        NullCheck.check(webServerPortS, new BadRequestException("WebServer Port ('WebServerPort') not specified in the request."));
        NullCheck.check(serverName, new BadRequestException("Server Name ('ServerName') not specified in the request."));
        NullCheck.check(themeBase, new BadRequestException("Theme Base ('ThemeBase') not specified in the request."));

        UUID serverUUID = UUID.fromString(variables.get("sender"));
        setOriginalSettings(serverUUID, webServerPortS, serverName, themeBase);

        Server bukkit = new Server(-1, serverUUID, serverName, address, -1);

        InfoSystem.getInstance().getConnectionSystem().sendInfoRequest(new SaveDBSettingsRequest(), bukkit);

        return DefaultResponses.SUCCESS.get();
    }

    private void setOriginalSettings(UUID serverUUID, String webServerPortS, String serverName, String themeBase) {
        Map<String, Object> settings = new HashMap<>();
        int webServerPort = Integer.parseInt(webServerPortS);
        settings.put("WebServerPort", webServerPort);
        settings.put("ServerName", serverName);
        settings.put("ThemeBase", themeBase);
        Settings.serverSpecific().addOriginalBukkitSettings(serverUUID, settings);
    }
}