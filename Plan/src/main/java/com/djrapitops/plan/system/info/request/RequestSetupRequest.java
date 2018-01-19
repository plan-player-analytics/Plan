/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.system.webserver.response.Response;

import java.util.Map;

/**
 * InfoRequest for /plan m setup command.
 *
 * @author Rsl1122
 */
// TODO
public class RequestSetupRequest extends InfoRequestWithVariables {

    @Override
    public void placeDataToDatabase() {
        // Not Required with setup request.
    }

    @Override
    public Response handleRequest(Map<String, String> variables) {
        return null;
    }
}