/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.systems.webserver.response.PromptAuthorizationResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;

import java.io.IOException;

/**
 * HttpHandler for webserver request management.
 *
 * @author Rsl1122
 */
public class RequestHandler implements HttpHandler {


    private final ResponseHandler responseHandler;

    RequestHandler(IPlan plugin, WebServer webServer) {
        responseHandler = new ResponseHandler(plugin, webServer);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers responseHeaders = exchange.getResponseHeaders();
        Request request = new Request(exchange);

        try {
            Response response = responseHandler.getResponse(request);
            if (response instanceof PromptAuthorizationResponse) {
                responseHeaders.set("WWW-Authenticate", "Basic realm=\"/\";");
            }
            response.setResponseHeaders(responseHeaders);
            response.send(exchange);
        } finally {
            exchange.close();
        }
    }


}