package main.java.com.djrapitops.plan.ui.webserver.response;

import main.java.com.djrapitops.plan.ui.html.DataRequestHandler;
import main.java.com.djrapitops.plan.ui.theme.Theme;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class AnalysisPageResponse extends Response {

    public AnalysisPageResponse(DataRequestHandler h) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(Theme.replaceColors(h.getServerHtml()));
    }
}
