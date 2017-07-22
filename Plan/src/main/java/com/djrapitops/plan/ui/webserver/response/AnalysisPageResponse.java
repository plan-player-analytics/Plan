package main.java.com.djrapitops.plan.ui.webserver.response;

import java.io.OutputStream;
import main.java.com.djrapitops.plan.ui.html.DataRequestHandler;

/**
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class AnalysisPageResponse extends Response {

    public AnalysisPageResponse(OutputStream output, DataRequestHandler h) {
        super(output);
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(h.getAnalysisHtml());
    }
}
