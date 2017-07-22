package main.java.com.djrapitops.plan.ui.webserver.response;

import java.io.OutputStream;

/**
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class RedirectResponse extends Response {

    public RedirectResponse(OutputStream output, String direct) {
        super(output);
        super.setHeader("HTTP/1.1 302 Found");
        super.setContent("Location: " + direct);
    }
}
