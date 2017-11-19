package main.java.com.djrapitops.plan.systems.webserver.response;

import main.java.com.djrapitops.plan.utilities.html.Html;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class ForbiddenResponse extends ErrorResponse {
    public ForbiddenResponse() {
        super.setHeader("HTTP/1.1 403 Forbidden");
        super.setTitle(Html.FONT_AWESOME_ICON.parse("hand-stop-o")+" 403 Forbidden - Access Denied");
    }

    public ForbiddenResponse(String msg) {
        super.setHeader("HTTP/1.1 404 Not Found");
        super.setTitle("403 Forbidden - Access Denied");
        super.setParagraph(msg);
        super.replacePlaceholders();
    }
}
