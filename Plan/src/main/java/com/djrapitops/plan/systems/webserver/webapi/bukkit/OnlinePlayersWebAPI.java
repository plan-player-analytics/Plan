/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.JsonResponse;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;
import org.bukkit.Server;

import java.util.Map;

/**
 * @author Fuzzlemann
 */
public class OnlinePlayersWebAPI implements WebAPI {
    @Override
    public Response onResponse(IPlan plugin, Map<String, String> variables) {
        Server server = ((Plan) plugin).getServer();

        return new JsonResponse(server.getOnlinePlayers() + "/" + server.getMaxPlayers());
    }
}
