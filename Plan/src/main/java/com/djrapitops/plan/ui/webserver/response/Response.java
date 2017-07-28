package main.java.com.djrapitops.plan.ui.webserver.response;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsExchange;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.ui.webserver.Request;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public abstract class Response implements HttpHandler {


    private final OutputStream output;

    private String header;
    private String content;

    /**
     * Class Constructor.
     *
     * @param output Website OutputStream to write the response to.
     */
    public Response(OutputStream output) {
        this.output = output;
    }

    /**
     * Writes the HTML to the OutputStream according to the requested page.
     *
     * @throws IOException
     */
    public void sendStaticResource() throws IOException {
        String response = getResponse();
//        Log.debug("Response: " + response); // Responses should not be logged, html content large.
        output.write(response.getBytes());
        output.flush();
    }

    public String getResponse() {
        return header + "\r\n"
                + "Content-Type: text/html;\r\n"
                + "Content-Length: " + content.length() + "\r\n"
                + "\r\n"
                + content;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public void handle(HttpExchange xghng) throws IOException {
        Log.debug("Recieved HTTP Exchange");
        Log.debug(xghng.toString());
        HttpsExchange exchange = (HttpsExchange) xghng;
        try {
            Headers headers = exchange.getRequestHeaders();
            Request req = new Request(exchange.getRequestBody());
            Log.debug(req.toString());
//            headers.set("Content-Type", "text/html");

            exchange.sendResponseHeaders(getCode(), content.length());
            Log.debug("Content:");
            Log.debug(content);
            OutputStream os = exchange.getResponseBody();
            os.write(content.getBytes());
            os.close();
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            throw e;
        }
    }

    private int getCode() {
        if (header == null) {
            return 500;
        } else {
            return Integer.parseInt(header.split(" ")[1]);
        }
    }
}
