/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response.api;

import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.ResponseType;
import com.google.gson.Gson;

/**
 * @author Fuzzlemann
 */
public class JsonResponse extends Response {

    public <T> JsonResponse(T object) {
        super(ResponseType.JSON);
        Gson gson = new Gson();

        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(gson.toJson(object));
    }
}
