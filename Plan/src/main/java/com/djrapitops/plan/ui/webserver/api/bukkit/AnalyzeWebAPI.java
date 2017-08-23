/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.ui.webserver.api.bukkit;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.PageCacheHandler;
import main.java.com.djrapitops.plan.ui.webserver.response.Response;
import main.java.com.djrapitops.plan.ui.webserver.response.api.SuccessResponse;
import main.java.com.djrapitops.plan.utilities.webserver.api.WebAPI;

import java.util.Map;

/**
 * @author Fuzzlemann
 */
public class AnalyzeWebAPI implements WebAPI {
    @Override
    public Response onResponse(Plan plan, Map<String, String> variables) {
        // TODO plan.getAnalysisCache().updateCache();
        return PageCacheHandler.loadPage("success", SuccessResponse::new);
    }
}
