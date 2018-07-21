/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.webserver.response.Response;

import java.util.Map;

/**
 * Represents a request that Plan servers can send each other.
 *
 * @author Rsl1122
 */
public interface InfoRequest {

    Response handleRequest(Map<String, String> variables) throws WebException;

    void runLocally() throws WebException;

}
