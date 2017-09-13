/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.systems.webapi.WebAPI;
import main.java.com.djrapitops.plan.systems.webapi.WebAPIManager;
import main.java.com.djrapitops.plan.systems.webserver.response.ForbiddenResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * //TODO Class Javadoc Comment
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

        if (args.length < 3) {
            String error = "API Method not specified";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        String method = args[2];
        String response = null;
        try (InputStream inputStream = request.getRequestBody()) {
            response = readPOSTRequest(inputStream);
        }

        if (response == null) {
            String error = "Error at reading the POST request." +
                    "Note that the Encoding must be ISO-8859-1.";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        Map<String, String> variables = readVariables(response);
        String key = variables.get("key");

        if (!checkKey(key)) {
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

        return api.onResponse(Plan.getInstance(), variables);
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

    private boolean checkKey(String key) {
        if (key == null) {
            return false;
        }

        UUID uuid = MiscUtils.getIPlan().getServerInfoManager().getServerUUID();
        UUID keyUUID;
        try {
            keyUUID = UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return uuid.equals(keyUUID);
    }

    private Map<String, String> readVariables(String response) {
        String[] variables = response.split("&");

        return Arrays.stream(variables)
                .map(variable -> variable.split("=", 2))
                .filter(splittedVariables -> splittedVariables.length == 2)
                .collect(Collectors.toMap(splittedVariables -> splittedVariables[0], splittedVariables -> splittedVariables[1], (a, b) -> b));
    }
}