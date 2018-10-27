/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response.errors;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.update.VersionCheckSystem;

import java.io.IOException;

/**
 * Response when Server is not found in database when attempting to InfoRequest.
 *
 * @author Rsl1122
 */
public class UnauthorizedServerResponse extends ErrorResponse {
    public UnauthorizedServerResponse(String message, VersionCheckSystem versionCheckSystem, PlanFiles files) throws IOException {
        super(versionCheckSystem, files);
        super.setHeader("HTTP/1.1 412 Unauthorized");
        super.setTitle("Unauthorized Server");
        super.setParagraph(message);
        super.replacePlaceholders();
    }
}
