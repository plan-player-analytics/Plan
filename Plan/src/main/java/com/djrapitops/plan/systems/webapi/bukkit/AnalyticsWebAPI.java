/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webapi.bukkit;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.webapi.WebAPI;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.api.JsonResponse;

import java.util.Map;

/**
 * @author Fuzzlemann
 */
public class AnalyticsWebAPI implements WebAPI {
    @Override
    public Response onResponse(Plan plan, Map<String, String> variables) {
        InformationManager infoManager = plan.getInfoManager();

        if (!infoManager.isAnalysisCached()) {
            return PageCache.loadPage("No Analysis Data", () -> new BadRequestResponse("No analysis data available"));
        }

        return new JsonResponse(infoManager.getAnalysisHtml());
    }
}
