package main.java.com.djrapitops.plan.ui.webserver.response;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

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
    public void handle(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(getCode(), content.length());

        OutputStream os = exchange.getResponseBody();
        os.write(content.getBytes());
        os.close();
    }

    private int getCode() {
        if (header == null) {
            return 500;
        } else {
            return Integer.parseInt(header.split(" ")[1]);
        }
    }
}
