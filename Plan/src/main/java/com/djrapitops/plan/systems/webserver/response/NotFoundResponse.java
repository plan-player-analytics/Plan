package main.java.com.djrapitops.plan.systems.webserver.response;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class NotFoundResponse extends Response {

    public NotFoundResponse() {
        super.setHeader("HTTP/1.1 404 Not Found");
        super.setContent("<h1>404 Not Found</h1><p>Page does not exist.</p>");
    }

    public NotFoundResponse(String msg) {
        super.setHeader("HTTP/1.1 404 Not Found");
        super.setContent("<h1>404 Not Found</h1><p>" + msg + "</p>");
    }
}
