/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.common.system.webserver.response.api;

import com.djrapitops.plan.common.system.webserver.response.Response;

/**
 * @author Fuzzlemann
 */
public class SuccessResponse extends Response {

    public SuccessResponse() {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent("Success");
    }
}
