/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.ui.webserver.api.bukkit;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.PageCacheHandler;
import main.java.com.djrapitops.plan.ui.webserver.response.Response;
import main.java.com.djrapitops.plan.ui.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.utilities.webserver.api.WebAPI;

import java.util.Map;

/**
 * @author Fuzzlemann
 */
public class AnalyticsWebAPI implements WebAPI {
    @Override
    public Response onResponse(Plan plan, Map<String, String> variables) {
        String identifier = "analysisJson";

        if (!PageCacheHandler.isCached(identifier)) {
            return PageCacheHandler.loadPage("No Analysis Data", () -> new BadRequestResponse("No analysis data available"));
        }

        return PageCacheHandler.loadPage(identifier);
    }
}
