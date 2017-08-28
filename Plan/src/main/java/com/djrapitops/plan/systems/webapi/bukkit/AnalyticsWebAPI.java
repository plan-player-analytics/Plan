/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webapi.bukkit;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.systems.webapi.WebAPI;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.BadRequestResponse;

import java.util.Map;

/**
 * @author Fuzzlemann
 */
public class AnalyticsWebAPI implements WebAPI {
    @Override
    public Response onResponse(Plan plan, Map<String, String> variables) {
        String identifier = "analysisJson";

        if (!PageCache.isCached(identifier)) {
            return PageCache.loadPage("No Analysis Data", () -> new BadRequestResponse("No analysis data available"));
        }

        return PageCache.loadPage(identifier);
    }
}
