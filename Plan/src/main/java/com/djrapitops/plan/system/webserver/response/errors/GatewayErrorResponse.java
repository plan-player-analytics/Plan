/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response.errors;

/**
 * ErrorResponse for GatewayException.
 *
 * @author Rsl1122
 */
public class GatewayErrorResponse extends ErrorResponse {

    public GatewayErrorResponse(String message) {
        super.setHeader("HTTP/1.1 504 Gateway Error");
        super.setTitle("Failed to Connect (Gateway Error)");
        super.setParagraph(message);
        super.replacePlaceholders();
    }
}
