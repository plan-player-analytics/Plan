/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.systems.webserver.response.*;
import main.java.com.djrapitops.plan.systems.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPIManager;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.html.Html;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

        if ("/favicon.ico".equals(target)) {
            return PageCache.loadPage("Redirect: favicon", () -> new RedirectResponse("https://puu.sh/tK0KL/6aa2ba141b.ico"));
        }
        if (target.endsWith(".css")) {
            return PageCache.loadPage(target + "css", () -> new CSSResponse("main.css"));
        }

        if (args.length < 2 || !"api".equals(args[1])) {
            String address = MiscUtils.getIPlan().getInfoManager().getWebServerAddress() + target;
            String link = Html.LINK.parse(address, address);
            return PageCache.loadPage("Non-API Request", () -> new NotFoundResponse("WebServer is in WebAPI-only mode, " +
                    "connect to the Bungee server instead: " + link));
        }

        if (args.length < 3) {
            String error = "API Method not specified";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        String method = args[2];
        String requestBody;
        try (InputStream inputStream = request.getRequestBody()) {
            requestBody = readPOSTRequest(inputStream);
        }

        if (requestBody == null) {
            String error = "Error at reading the POST request." +
                    "Note that the Encoding must be ISO-8859-1.";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        Map<String, String> variables = readVariables(requestBody);
        String sender = variables.get("sender");
        Log.debug("Received WebAPI Request" + target + " from " + sender);
        if (!checkKey(sender)) {
            String error = "Server Key not given or invalid";
            return PageCache.loadPage(error, () -> {
                ForbiddenResponse forbidden = new ForbiddenResponse();
                forbidden.setContent(error);
                return forbidden;
            });
        }

        WebAPI api = webAPI.getAPI(method);

        if (api == null) {
            String error = "API Method not found";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        Response response = api.processRequest(MiscUtils.getIPlan(), variables);

        Log.debug(response.toString());

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
            List<UUID> uuids = MiscUtils.getIPlan().getDB().getServerTable().getServerUUIDs();
            UUID keyUUID = UUID.fromString(sender);
            return uuids.contains(keyUUID);
        } catch (SQLException | IllegalArgumentException e) {
            return false;
        }
    }

    private Map<String, String> readVariables(String requestBody) {
        String[] variables = requestBody.split("&");

        return Arrays.stream(variables)
                .map(variable -> variable.split("=", 2))
                .filter(splitVariables -> splitVariables.length == 2)
                .collect(Collectors.toMap(splitVariables -> splitVariables[0], splitVariables -> splitVariables[1], (a, b) -> b));
    }
}