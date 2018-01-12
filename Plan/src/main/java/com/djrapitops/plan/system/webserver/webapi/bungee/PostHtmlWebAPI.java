/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.webapi.bungee;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.WebAPIException;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.pagecache.ResponseCache;
import com.djrapitops.plan.system.webserver.pagecache.PageId;
import com.djrapitops.plan.system.webserver.response.pages.AnalysisPageResponse;
import com.djrapitops.plan.system.webserver.response.pages.InspectPageResponse;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.webapi.WebAPI;
import com.djrapitops.plan.systems.info.InformationManager;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plugin.api.utility.log.Log;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * WebAPI for posting Html pages such as Inspect or server pages.
 *
 * @author Rsl1122
 */
public class PostHtmlWebAPI extends WebAPI {

    @Override
    public Response onRequest(PlanPlugin plugin, Map<String, String> variables) {
        try {
            String htmlVariable = variables.get("html");
            if (htmlVariable == null) {
                return badRequest("Html was null");
            }
            if (!htmlVariable.startsWith("<!DOCTYPE html>")) {
                String[] split = htmlVariable.split("<!DOCTYPE html>", 2);
                if (split.length <= 1) {
                    Log.debug(htmlVariable);
                    return badRequest("Html did not start with <!DOCTYPE html>");
                }
                htmlVariable = "<!DOCTYPE html>" + split[1];
            }
            String html = htmlVariable;

            String target = variables.get("target");
            InformationManager infoManager = plugin.getInfoManager();
            switch (target) {
                case "inspectPage":
                    String uuid = variables.get("uuid");

                    Map<String, String> map = new HashMap<>();
                    map.put("networkName", Settings.BUNGEE_NETWORK_NAME.toString());

                    ResponseCache.cacheResponse(PageId.PLAYER.of(uuid), () -> new InspectPageResponse(infoManager, UUID.fromString(uuid), StrSubstitutor.replace(html, map)));
                    if (Settings.ANALYSIS_EXPORT.isTrue()) {
                        HtmlExport.exportPlayer(plugin, UUID.fromString(uuid));
                    }
                    break;
                case "analysisPage":
                    String sender = variables.get("sender");
                    ResponseCache.cacheResponse(PageId.SERVER.of(sender), () -> new AnalysisPageResponse(html));
                    if (Settings.ANALYSIS_EXPORT.isTrue()) {
                        HtmlExport.exportServer(plugin, UUID.fromString(sender));
                    }
                    break;
                default:
                    return badRequest("Faulty Target");
            }
            return success();
        } catch (NullPointerException e) {
            return badRequest(e.toString());
        }
    }

    @Override
    public void sendRequest(String address) throws WebAPIException {
        throw new IllegalStateException("Wrong method call for this WebAPI, call sendRequest(String, UUID, UUID) instead.");
    }

    public void sendInspectHtml(String address, UUID uuid, String html) throws WebAPIException {
        addVariable("uuid", uuid.toString());
        addVariable("target", "inspectPage");
        addVariable("html", html);
        super.sendRequest(address);
    }

    public void sendAnalysisHtml(String address, String html) throws WebAPIException {
        addVariable("html", html);
        addVariable("target", "analysisPage");
        super.sendRequest(address);
    }
}