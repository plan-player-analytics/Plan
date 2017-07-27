package main.java.com.djrapitops.plan.ui.webserver.response;

import java.io.OutputStream;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class ForbiddenResponse extends Response {

    public ForbiddenResponse(OutputStream output) {
        super(output);
        super.setHeader("HTTP/1.1 403 Forbidden");
    }
}
