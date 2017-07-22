package main.java.com.djrapitops.plan.ui.webserver.response;

import java.io.OutputStream;
import main.java.com.djrapitops.plan.ui.html.Html;

/**
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class InternalErrorResponse extends Response {

    public InternalErrorResponse(OutputStream output, Throwable e, String cause) {
        super(output);
        super.setHeader("HTTP/1.1 500 Internal Error");
        StringBuilder content = new StringBuilder();
        content.append("<h1>500 Internal Error occurred</h1>");
        content.append("<p>Please report this issue here: </p>");
        content.append(Html.LINK.parse("https://github.com/Rsl1122/Plan-PlayerAnalytics/issues", "Issues"));
        content.append("<p>");
        content.append(e).append(" | ").append(cause);
        for (Object element : e.getStackTrace()) {
            content.append("<br>");
            content.append("  ").append(element);
        }
        content.append("</p>");
        super.setContent(content.toString());
    }
}
