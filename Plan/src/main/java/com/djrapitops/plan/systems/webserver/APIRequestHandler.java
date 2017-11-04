/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver;

import com.djrapitops.plugin.api.utility.log.Log;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPIManager;

import java.io.IOException;

/**
 * HttpHandler for webserver request management.
 *
 * @author Rsl1122
 */
public class APIRequestHandler implements HttpHandler {

    private final APIResponseHandler responseHandler;

    APIRequestHandler(WebAPIManager webAPI) {
        responseHandler = new APIResponseHandler(webAPI);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers responseHeaders = exchange.getResponseHeaders();
        Request request = new Request(exchange);
        try {
            Response response = responseHandler.getAPIResponse(request);
            response.setResponseHeaders(responseHeaders);
            response.send(exchange);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
        } finally {
            exchange.close();
        }
    }


}