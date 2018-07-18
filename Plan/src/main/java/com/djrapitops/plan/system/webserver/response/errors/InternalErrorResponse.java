package com.djrapitops.plan.system.webserver.response.errors;

import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Icon;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class InternalErrorResponse extends ErrorResponse {

    public InternalErrorResponse(String cause, Throwable e) {
        super.setHeader("HTTP/1.1 500 Internal Error");

        super.setTitle(Icon.called("bug") + " 500 Internal Error occurred");

        StringBuilder paragraph = new StringBuilder();
        paragraph.append("Please report this issue here: ");
        paragraph.append(Html.LINK.parse("https://github.com/Rsl1122/Plan-PlayerAnalytics/issues", "Issues"));
        paragraph.append("<br><br><pre>");
        paragraph.append(e).append(" | ").append(cause);

        for (StackTraceElement element : e.getStackTrace()) {
            paragraph.append("<br>");
            paragraph.append("    ").append(element);
        }
        if (e.getCause() != null) {
            appendCause(e.getCause(), paragraph);
        }

        paragraph.append("</pre>");

        super.setParagraph(paragraph.toString());
        super.replacePlaceholders();
    }

    private void appendCause(Throwable cause, StringBuilder paragraph) {
        paragraph.append("<br>Caused by: ").append(cause);
        for (StackTraceElement element : cause.getStackTrace()) {
            paragraph.append("<br>");
            paragraph.append("    ").append(element);
        }
        if (cause.getCause() != null) {
            appendCause(cause.getCause(), paragraph);
        }
    }
}
