/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.webapi.bungee;


import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.webapi.WebAPI;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.utilities.Verify;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class PostOriginalBukkitSettingsWebAPI extends WebAPI {

    @Override
    public Response onRequest(PlanPlugin plugin, Map<String, String> variables) {
        if (Check.isBukkitAvailable()) {
            return badRequest("Called a Bukkit Server");
        }
        Map<String, Object> settings = new HashMap<>();

        String webServerPortS = variables.get("WebServerPort");
        String serverName = variables.get("ServerName");
        String themeBase = variables.get("ThemeBase");
        if (!Verify.notNull(webServerPortS, serverName, themeBase)) {
            return badRequest("Not all variables were set");
        }

        int webServerPort = Integer.parseInt(webServerPortS);
        settings.put("WebServerPort", webServerPort);
        settings.put("ServerName", serverName);
        settings.put("ThemeBase", themeBase);
        Settings.serverSpecific().addOriginalBukkitSettings((PlanBungee) plugin, UUID.fromString(variables.get("sender")), settings);
        return success();
    }

    @Override
    public void sendRequest(String address) throws WebException {
        addVariable("WebServerPort", Integer.toString(Settings.WEBSERVER_PORT.getNumber()));
        addVariable("ServerName", Settings.SERVER_NAME.toString().replaceAll("[^a-zA-Z0-9_\\s]", "_"));
        addVariable("ThemeBase", Settings.THEME_BASE.toString());
        super.sendRequest(address);
    }
}