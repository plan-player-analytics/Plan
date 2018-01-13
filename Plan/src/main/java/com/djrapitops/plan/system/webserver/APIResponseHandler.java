/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.system.webserver.pagecache.PageId;
import com.djrapitops.plan.system.webserver.pagecache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.CSSResponse;
import com.djrapitops.plan.system.webserver.response.JavaScriptResponse;
import com.djrapitops.plan.system.webserver.response.RedirectResponse;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.api.BadRequestResponse;
import com.djrapitops.plan.system.webserver.response.errors.ForbiddenResponse;
import com.djrapitops.plan.system.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plan.system.webserver.response.pages.DebugPageResponse;
import com.djrapitops.plan.system.webserver.webapi.WebAPI;
import com.djrapitops.plan.system.webserver.webapi.WebAPIManager;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles choosing of the correct API response to an API request.
 *
 * @author Rsl1122
 */
public class APIResponseHandler {

    private final WebAPIManager webAPI;

    public APIResponseHandler(WebAPIManager webAPI) {
        this.webAPI = webAPI;
    }

    Response getAPIResponse(Request request) throws IOException {
        String target = request.getTarget();
        String[] args = target.split("/");

        if ("/favicon.ico".equalsIgnoreCase(target)) {
            return ResponseCache.loadResponse(PageId.FAVICON_REDIRECT.id(), () -> new RedirectResponse("https://puu.sh/tK0KL/6aa2ba141b.ico"));
        }
        if ("/debug".equalsIgnoreCase(target)) {
            return new DebugPageResponse();
        }
        if (target.endsWith(".css")) {
            return ResponseCache.loadResponse(PageId.CSS.of(target), () -> new CSSResponse(target));
        }

        if (target.endsWith(".js")) {
            return ResponseCache.loadResponse(PageId.JS.of(target), () -> new JavaScriptResponse(target));
        }

        if (args.length < 2 || !"api".equals(args[1])) {
            String address = PlanPlugin.getInstance().getInfoManager().getWebServerAddress() + target;
            String link = Html.LINK.parse(address, address);
            return ResponseCache.loadResponse(PageId.ERROR.of("Non-API Request"), () -> new NotFoundResponse("WebServer is in WebAPI-only mode, " +
                    "connect to the Bungee server instead: " + link));
        }

        if (args.length < 3) {
            String error = "API Method not specified";
            return ResponseCache.loadResponse(PageId.ERROR.of(error), () -> new BadRequestResponse(error));
        }

        String method = args[2];
        String requestBody;
        try (InputStream inputStream = request.getRequestBody()) {
            requestBody = readPOSTRequest(inputStream);
        }

        if (requestBody == null) {
            String error = "Error at reading the POST request." +
                    "Note that the Encoding must be ISO-8859-1.";
            return ResponseCache.loadResponse(PageId.ERROR.of(error), () -> new BadRequestResponse(error));
        }

        Map<String, String> variables = WebAPI.readVariables(requestBody);
        String sender = variables.get("sender");
        Log.debug("Received WebAPI Request" + target + " from " + sender);

        boolean isPing = "pingwebapi".equalsIgnoreCase(method);
        boolean isSetupRequest = "requestsetupwebapi".equalsIgnoreCase(method);
        boolean isPostOriginalSettings = "postoriginalbukkitsettingswebapi".equalsIgnoreCase(method);
        boolean skipAuthCheck = isPing || isSetupRequest || isPostOriginalSettings;

        // TODO refactor to more methods
        if (!skipAuthCheck) {
            String accessKey = variables.get("accessKey");
            if (accessKey == null) {
                if (!checkKey(sender)) {
                    String error = "Server Key not given or invalid";
                    Log.debug("Request had invalid Server key: " + sender);
                    return ResponseCache.loadResponse(PageId.ERROR.of(error), () -> {
                        ForbiddenResponse forbidden = new ForbiddenResponse();
                        forbidden.setContent(error);
                        return forbidden;
                    });
                }
            } else {
                if (!webAPI.isAuthorized(accessKey)) {
                    String error = "Access Key invalid";
                    Log.debug("Request had invalid Access key: " + accessKey);
                    return ResponseCache.loadResponse(PageId.ERROR.of(error), () -> {
                        ForbiddenResponse forbidden = new ForbiddenResponse();
                        forbidden.setContent(error);
                        return forbidden;
                    });
                }
            }
        }

        WebAPI api = webAPI.getAPI(method);

        if (api == null) {
            String error = "API Method not found";
            Log.debug(error);
            return ResponseCache.loadResponse(PageId.ERROR.of(error), () -> new BadRequestResponse(error));
        }

        Response response = api.processRequest(PlanPlugin.getInstance(), variables);

        Log.debug("Response: " + response.getResponse().split("\r\n")[0]);

        return response;
    }

    private String readPOSTRequest(InputStream in) throws IOException {
        byte[] bytes;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        for (int n = in.read(buf); n > 0; n = in.read(buf)) {
            out.write(buf, 0, n);
        }

        bytes = out.toByteArray();

        try {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean checkKey(String sender) {
        if (sender == null) {
            return false;
        }

        try {
            List<UUID> uuids = PlanPlugin.getInstance().getDB().getServerTable().getServerUUIDs();
            UUID keyUUID = UUID.fromString(sender);
            return uuids.contains(keyUUID);
        } catch (SQLException | IllegalArgumentException e) {
            return false;
        }
    }
}