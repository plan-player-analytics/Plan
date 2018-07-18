package com.djrapitops.plan.system.webserver.response.errors;

import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class ForbiddenResponse extends ErrorResponse {
    public ForbiddenResponse() {
        super.setHeader("HTTP/1.1 403 Forbidden");
        super.setTitle(Icon.called("hand-paper").of(Family.REGULAR) + " 403 Forbidden - Access Denied");
    }

    public ForbiddenResponse(String msg) {
        super.setHeader("HTTP/1.1 403 Forbidden");
        super.setTitle(Icon.called("hand-paper").of(Family.REGULAR) + " 403 Forbidden - Access Denied");
        super.setParagraph(msg);
        super.replacePlaceholders();
    }
}
