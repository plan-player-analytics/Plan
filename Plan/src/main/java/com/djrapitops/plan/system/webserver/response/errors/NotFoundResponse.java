package com.djrapitops.plan.system.webserver.response.errors;

import com.djrapitops.plan.utilities.html.icon.Icon;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class NotFoundResponse extends ErrorResponse {

    public NotFoundResponse() {
        super.setHeader("HTTP/1.1 404 Not Found");
        super.setTitle(Icon.called("map-signs") + " 404 Not Found");
        super.setParagraph("Page does not exist.");
        super.replacePlaceholders();
    }

    public NotFoundResponse(String msg) {
        super.setHeader("HTTP/1.1 404 Not Found");
        super.setTitle(Icon.called("map-signs") + " 404 Not Found");
        super.setParagraph(msg);
        super.replacePlaceholders();
    }
}
