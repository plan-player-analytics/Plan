package com.djrapitops.plan.ui.webserver;

import com.djrapitops.plan.ui.DataRequestHandler;
import com.djrapitops.plan.utilities.UUIDFetcher;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 *
 * @author Rsl1122
 */
public class Response {

    private OutputStream output;
    private Request request;

    private DataRequestHandler requestHandler;

    public Response(OutputStream output, DataRequestHandler h) {
        this.output = output;
        requestHandler = h;
    }

    public void sendStaticResource() throws IOException {
        try {
            if (request == null) {
                return;
            }
            if (request.getUri() == null) {
                return;
            }
            String[] requestArgs = request.getUri().split("/");
            if (requestArgs.length < 1) {
                String errorMessage = "HTTP/1.1 403 Forbidden\r\n"
                        + "Content-Type: text/html\r\n"
                        + "Content-Length: 45\r\n"
                        + "\r\n"
                        + "<h1>403 Forbidden - Direct access not allowed</h1>";
                output.write(errorMessage.getBytes());
                return;
            }
            String command = requestArgs[1].toLowerCase();
            if (command.equals("player")) {
                if (requestArgs.length >= 3) {
                    UUID uuid = UUIDFetcher.getUUIDOf(requestArgs[2].trim());
                    if (uuid == null) {
                        String errorMessage = "HTTP/1.1 404 UUID not Found\r\n"
                                + "Content-Type: text/html\r\n"
                                + "Content-Length: 30\r\n"
                                + "\r\n"
                                + "<h1>404 - Player doesn't exist</h1>";
                        output.write(errorMessage.getBytes());
                        return;
                    }
                    if (requestHandler.checkIfCached(uuid)) {
                        String dataHtml = requestHandler.getDataHtml(uuid);
                        String htmlDef = "HTTP/1.1 Inspect\r\n"
                                + "Content-Type: text/html\r\n"
                                + "Content-Length: " + dataHtml.length() + "\r\n"
                                + "\r\n";
                        output.write((htmlDef+dataHtml).getBytes());
                        return;
                    }
                }
            } else if (command.equals("server")) {
                if (requestHandler.checkIfAnalysisIsCached()) {
                    String analysisHtml = requestHandler.getAnalysisHtml();
                    String htmlDef = "HTTP/1.1 Analysis\r\n"
                            + "Content-Type: text/html\r\n"
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

    public void setRequest(Request request) {
        this.request = request;
    }
}
