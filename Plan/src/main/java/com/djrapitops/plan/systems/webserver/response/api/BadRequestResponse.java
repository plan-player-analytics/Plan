/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.webserver.response.api;

import com.djrapitops.plan.systems.webserver.response.Response;

/**
 * @author Fuzzlemann
 */
public class BadRequestResponse extends Response {

    public BadRequestResponse(String error) {
        super.setHeader("HTTP/1.1 400 Bad Request");
        super.setContent(error);
    }
}
