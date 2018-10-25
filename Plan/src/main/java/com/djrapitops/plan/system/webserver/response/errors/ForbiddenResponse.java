package com.djrapitops.plan.system.webserver.response.errors;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.io.IOException;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class ForbiddenResponse extends ErrorResponse {
    public ForbiddenResponse(String msg, VersionCheckSystem versionCheckSystem, PlanFiles files) throws IOException {
        super(versionCheckSystem, files);
        super.setHeader("HTTP/1.1 403 Forbidden");
        super.setTitle(Icon.called("hand-paper").of(Family.REGULAR) + " 403 Forbidden - Access Denied");
        super.setParagraph(msg);
        super.replacePlaceholders();
    }
}
