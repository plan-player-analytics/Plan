/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.pages.DebugPage;

/**
 * WebServer response for /debug-page used for easing issue reporting.
 *
 * @author Rsl1122
 */
public class DebugPageResponse extends ErrorResponse {

    public DebugPageResponse(DebugPage debugPage) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setTitle(Icon.called("bug") + " Debug Information");
        super.setParagraph(debugPage.toHtml());
        replacePlaceholders();
    }

}
