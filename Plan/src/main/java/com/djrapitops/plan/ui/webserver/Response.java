package com.djrapitops.plan.ui.webserver;

import com.djrapitops.plan.ui.DataRequestHandler;
import com.djrapitops.plan.utilities.UUIDFetcher;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import main.java.com.djrapitops.plan.Settings;

/**
 *
 * @author Rsl1122
 */
public class Response {

    private OutputStream output;
    private Request request;

    private final DataRequestHandler requestHandler;

    /**
     * Class Constructor.
     *
     * @param output Website outputstream to write the response to.
     * @param h Current Instance of DataRequestHandler
     */
    public Response(OutputStream output, DataRequestHandler h) {
        this.output = output;
        requestHandler = h;
    }

    /**
     * Wrties the HTML to the Outputstream according to the requested page.
     *
     * @throws IOException
     */
    public void sendStaticResource() throws IOException {
        try {
            if (request == null) {
                return;
            }
            if (request.getUri() == null) {
                return;
            }
            String[] requestArgs = request.getUri().split("/");
            boolean forbidden = false;
            String securityCode = "";
            if (requestArgs.length < 1) {
                forbidden = true;
            } else {
                securityCode = requestArgs[1];
            }
            if (!securityCode.equals(Settings.SECURITY_CODE + "")) {
                forbidden = true;
            }
            if (forbidden) {
                String errorMessage = "HTTP/1.1 403 Forbidden\r\n"
                        + "Content-Type: text/html\r\n"
                        + "Content-Length: 38\r\n"
                        + "\r\n"
                        + "<h1>403 Forbidden - Access Denied</h1>";
                output.write(errorMessage.getBytes());
                return;
            }
            String command = requestArgs[2].toLowerCase();
            if (command.equals("player")) {
                if (requestArgs.length >= 3) {
                    String playerName = requestArgs[3].trim();
                    UUID uuid = UUIDFetcher.getUUIDOf(playerName);
                    if (uuid == null) {
                        String errorMessage = "HTTP/1.1 404 UUID not Found\r\n"
                                + "Content-Type: text/html;\r\n"
                                + "Content-Length: 30\r\n"
                                + "\r\n"
                                + "<h1>404 - Player doesn't exist</h1>";
                        output.write(errorMessage.getBytes());
                        return;
                    }
                    if (requestHandler.checkIfCached(uuid)) {
                        try {
                            String dataHtml = requestHandler.getDataHtml(uuid);
                            String htmlDef = "HTTP/1.1 OK\r\n"
                                    + "Content-Type: text/html; charset=utf-8\r\n"
                                    + "Content-Length: " + dataHtml.length() + "\r\n"
                                    + "\r\n";
                            output.write((htmlDef + dataHtml).getBytes());
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            String errorMessage = "HTTP/1.1 404 Error\r\n"
                                    + "Content-Type: text/html;\r\n"
                                    + "Content-Length: 30\r\n"
                                    + "\r\n"
                                    + "<h1>404 - Error has occurred..</h1>";
                            output.write(errorMessage.getBytes());
                        }
                        return;
                    }
                }
            } else if (command.equals("server")) {
                if (requestHandler.checkIfAnalysisIsCached()) {
                    String analysisHtml = requestHandler.getAnalysisHtml();
                    String htmlDef = "HTTP/1.1 OK\r\n"
                            + "Content-Type: text/html; charset=utf-8\r\n"
                            + "Content-Length: " + analysisHtml.length() + "\r\n"
                            + "\r\n";
                    output.write((htmlDef + analysisHtml).getBytes());
                    return;
                }
            }
            String errorMessage = "HTTP/1.1 404 UserData not Found\r\n"
                    + "Content-Type: text/html\r\n"
                    + "Content-Length: 35\r\n"
                    + "\r\n"
                    + "<h1>404 Data was not found in cache</h1>";
            output.write(errorMessage.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the HTML Request to get response for.
     *
     * @param request Request.
     */
    public void setRequest(Request request) {
        this.request = request;
    }
}
