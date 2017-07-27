package main.java.com.djrapitops.plan.ui.webserver.response;

import main.java.com.djrapitops.plan.ui.html.DataRequestHandler;

import java.io.OutputStream;
import java.util.UUID;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class InspectPageResponse extends Response {

    public InspectPageResponse(OutputStream output, DataRequestHandler h, UUID uuid) {
        super(output);
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(h.getInspectHtml(uuid));
    }
}
