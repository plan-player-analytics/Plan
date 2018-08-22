/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.system.webserver.response.errors.ForbiddenResponse;

/**
 * Enum containing default responses that don't need to be cached because they're always the same.
 *
 * @author Rsl1122
 */
public enum DefaultResponses {
    BASIC_AUTH(PromptAuthorizationResponse.getBasicAuthResponse()),
    SUCCESS(new TextResponse("Success")),
    FORBIDDEN(new ForbiddenResponse("Your user is not authorized to view this page.<br>"
            + "If you believe this is an error contact staff to change your access level."));

    private final Response response;

    DefaultResponses(Response response) {
        this.response = response;
    }

    public Response get() {
        return response;
    }
}
