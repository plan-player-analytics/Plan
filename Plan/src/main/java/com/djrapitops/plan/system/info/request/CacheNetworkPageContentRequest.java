/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.system.webserver.response.Response;

import java.util.Map;

/**
 * InfoRequest for caching Network page parts to ResponseCache of receiving server.
 *
 * @author Rsl1122
 */
public class CacheNetworkPageContentRequest implements InfoRequest {

    @Override
    public void placeDataToDatabase() {
        // TODO
    }

    @Override
    public Response handleRequest(Map<String, String> variables) {
        return null; // TODO
    }
}