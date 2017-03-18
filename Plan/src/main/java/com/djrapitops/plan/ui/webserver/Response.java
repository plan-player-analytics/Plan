package main.java.com.djrapitops.plan.ui.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.ui.DataRequestHandler;
import main.java.com.djrapitops.plan.utilities.UUIDFetcher;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

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
            if (requestArgs.length <= 2) {
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
                        + "Content-Length: 98\r\n"
                        + "\r\n"
                        + "<h1>403 Forbidden - Access Denied</h1><p>Make sure you're accessing the link given by a command</p>";
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
                            String dataHtml = requestHandler.getInspectHtml(uuid);
                            String htmlDef = "HTTP/1.1 200 OK\r\n"
                                    + "Content-Type: text/html; charset=utf-8\r\n"
                                    + "Content-Length: " + dataHtml.length() + "\r\n"
                                    + "\r\n";
                            output.write((htmlDef + dataHtml).getBytes());
                        } catch (NullPointerException e) {
                            getPlugin(Plan.class).toLog(this.getClass().getName(), e);
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
                    String htmlDef = "HTTP/1.1 200 OK\r\n"
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
            getPlugin(Plan.class).toLog(this.getClass().getName(), e);
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
