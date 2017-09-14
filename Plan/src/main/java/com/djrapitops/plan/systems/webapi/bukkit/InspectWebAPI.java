/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webapi.bukkit;

import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.systems.webapi.WebAPI;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.api.SuccessResponse;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;

import java.util.Map;
import java.util.UUID;

/**
 * @author Fuzzlemann
 */
public class InspectWebAPI implements WebAPI {
    @Override
    public Response onResponse(IPlan plugin, Map<String, String> variables) {
        String playerString = variables.get("player");

        if (playerString == null) {
            String error = "Player String not included";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        UUID uuid = UUIDUtility.getUUIDOf(playerString);

        if (uuid == null) {
            String error = "UUID not found";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        plugin.getInfoManager().cachePlayer(uuid);

        return PageCache.loadPage("success", SuccessResponse::new);
    }
}
