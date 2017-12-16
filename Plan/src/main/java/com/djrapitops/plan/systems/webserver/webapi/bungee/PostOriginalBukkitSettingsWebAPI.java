/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bungee;


import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.settings.Settings;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;

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
    public Response onRequest(IPlan plugin, Map<String, String> variables) {
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
    public void sendRequest(String address) throws WebAPIException {
        addVariable("WebServerPort", Integer.toString(Settings.WEBSERVER_PORT.getNumber()));
        addVariable("ServerName", Settings.SERVER_NAME.toString().replaceAll("[^a-zA-Z0-9_\\s]", "_"));
        addVariable("ThemeBase", Settings.THEME_BASE.toString());
        super.sendRequest(address);
    }
}