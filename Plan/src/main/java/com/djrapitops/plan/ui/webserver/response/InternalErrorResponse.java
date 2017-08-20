package main.java.com.djrapitops.plan.ui.webserver.response;

import main.java.com.djrapitops.plan.ui.html.Html;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class InternalErrorResponse extends Response {

    public InternalErrorResponse(Throwable e, String cause) {
        StringBuilder content = new StringBuilder();

        super.setHeader("HTTP/1.1 500 Internal Error");

        content.append("<h1>500 Internal Error occurred</h1>");
        content.append("<p>Please report this issue here: </p>");
        content.append(Html.LINK.parse("https://github.com/Rsl1122/Plan-PlayerAnalytics/issues", "Issues"));
        content.append("<p>");
        content.append(e).append(" | ").append(cause);

        for (StackTraceElement element : e.getStackTrace()) {
            content.append("<br>");
            content.append("  ").append(element);
        }

        content.append("</p>");

        super.setContent(content.toString());
    }
}
