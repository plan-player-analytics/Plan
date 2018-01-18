/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.system.webserver.response.Response;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest to generate Analysis page HTML at the receiving end.
 *
 * @author Rsl1122
 */
public class GenerateAnalysisPageRequest extends InfoRequestWithVariables {

    public GenerateAnalysisPageRequest(UUID serverUUID) {
        variables.put("server", serverUUID.toString());
    }

    @Override
    public void placeDataToDatabase() {
        // No data required in a Generate request
    }

    @Override
    public Response handleRequest(Map<String, String> variables) {

        return null;
    }
}