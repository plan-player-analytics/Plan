/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webapi.universal;

import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.systems.webapi.WebAPI;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.SuccessResponse;

import java.util.Map;

/**
 * @author Fuzzlemann
 */
public class PingWebAPI implements WebAPI {
    @Override
    public Response onResponse(IPlan plugin, Map<String, String> variables) {
        return PageCache.loadPage("success", SuccessResponse::new);
    }
}