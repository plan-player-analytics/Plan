/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response.api;

import com.djrapitops.plan.system.webserver.response.Response;

/**
 * @author Fuzzlemann
 */
public class BadRequestResponse extends Response {

    public BadRequestResponse(String error) {
        super.setHeader("HTTP/1.1 400 Bad Request " + error);
        super.setContent("400 Bad Request: " + error);
    }
}
