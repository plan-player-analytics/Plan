/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response.errors;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.update.VersionCheckSystem;

import java.io.IOException;

/**
 * ErrorResponse for GatewayException.
 *
 * @author Rsl1122
 */
public class GatewayErrorResponse extends ErrorResponse {

    public GatewayErrorResponse(String message, VersionCheckSystem versionCheckSystem, PlanFiles files) throws IOException {
        super(versionCheckSystem, files);
        super.setHeader("HTTP/1.1 504 Gateway Error");
        super.setTitle("Failed to Connect (Gateway Error)");
        super.setParagraph(message);
        super.replacePlaceholders();
    }
}
