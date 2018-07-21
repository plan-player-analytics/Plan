package com.djrapitops.plan.system.webserver.response;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class RedirectResponse extends Response {

    public RedirectResponse(String direct) {
        super.setHeader("HTTP/1.1 302 Found");
        super.setContent("Location: " + direct);
    }
}
