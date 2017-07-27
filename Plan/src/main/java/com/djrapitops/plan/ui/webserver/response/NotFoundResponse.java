package main.java.com.djrapitops.plan.ui.webserver.response;

import java.io.OutputStream;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class NotFoundResponse extends Response {

    public NotFoundResponse(OutputStream output) {
        super(output);
        super.setHeader("HTTP/1.1 404 Not Found");
        super.setContent("<h1>404 Not Found</h1><p>Page does not exist.</p>");
    }

    public NotFoundResponse(OutputStream output, String msg) {
        super(output);
        super.setHeader("HTTP/1.1 404 Not Found");
        super.setContent("<h1>404 Not Found</h1><p>" + msg + "</p>");
    }
}
