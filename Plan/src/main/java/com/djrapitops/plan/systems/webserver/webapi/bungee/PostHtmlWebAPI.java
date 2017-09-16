/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bungee;

import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.AnalysisPageResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.InspectPageResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.api.SuccessResponse;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;

import java.util.Map;
import java.util.UUID;

/**
 * WebAPI for posting Html pages such as Inspect, players or server pages.
 *
 * @author Rsl1122
 */
public class PostHtmlWebAPI extends WebAPI {

    private final IPlan plugin;

    public PostHtmlWebAPI(IPlan plugin) {
        this.plugin = plugin;
    }

    @Override
    public Response onResponse(IPlan plugin, Map<String, String> variables) {
        try {
            String html = variables.get("html");
            String target = variables.get("target");
            InformationManager infoManager = plugin.getInfoManager();
            switch (target) {
                case "inspectPage":
                    String uuid = variables.get("uuid");
                    PageCache.loadPage("inspectPage:" + uuid, () -> new InspectPageResponse(infoManager, UUID.fromString(uuid), html));
                    break;
                case "analysisPage":
                    PageCache.loadPage("analysisPage:" + variables.get("sender"), () -> new AnalysisPageResponse(html));
                default:
                    String error = "Faulty Target";
                    return PageCache.loadPage(error, () -> new BadRequestResponse(error));
            }
            return PageCache.loadPage("success", SuccessResponse::new);
        } catch (NullPointerException e) {
            return PageCache.loadPage(e.toString(), () -> new BadRequestResponse(e.toString()));
        }
    }
}