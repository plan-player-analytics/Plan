package main.java.com.djrapitops.plan.ui.webserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.ui.DataRequestHandler;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class WebSocketServer {

    private final int PORT;
    private boolean ENABLED = false;
    private ServerSocket server;

    private final Plan plugin;
    private final DataRequestHandler dataReqHandler;

    private boolean shutdown;

    /**
     * Class Constructor.
     *
     * Initializes DataRequestHandler
     *
     * @param plugin Current instance of Plan
     */
    public WebSocketServer(Plan plugin) {
        this.plugin = plugin;
        this.PORT = Settings.WEBSERVER_PORT.getNumber();
        shutdown = false;
        dataReqHandler = new DataRequestHandler(plugin);
    }

    /**
     * Starts up the Webserver in a Asyncronous thread.
     */
    public void initServer() {
        //Server is already enabled stop code
        if (ENABLED) {
            return;
        }
        Log.info(Phrase.WEBSERVER_INIT + "");
        try {
            //Setup server
            try {
                server = new ServerSocket(PORT, 1, InetAddress.getByName(Settings.WEBSERVER_IP.toString()));
            } catch (IOException e) {
                System.exit(1);
            }
            //Run server in seperate thread
            (new BukkitRunnable() {
                @Override
                public void run() {
                    while (!shutdown) {
                        Socket socket = null;
                        InputStream input = null;
                        OutputStream output = null;
                        try {
                            socket = server.accept();
                            input = socket.getInputStream();
                            output = socket.getOutputStream();
                            Request request = new Request(input);
                            request.parse();
                            Response response = new Response(output, dataReqHandler);
                            response.setRequest(request);
                            response.sendStaticResource();
                        } catch (IOException e) {                            
                        } finally {
                            Closeable[] close = new Closeable[]{input, output, socket};
                            for (Closeable closeable : close) {
                                try {
                                    if (closeable != null) {
                                        closeable.close();
                                    }
                                } catch (IOException e) {
                                    
                                }
                            }
                        }
                    }
                    this.cancel();
                }
            }).runTaskAsynchronously(plugin);

            ENABLED = true;

            Log.info(Phrase.WEBSERVER_RUNNING.parse(server.getLocalPort() + ""));
        } catch (IllegalArgumentException | IllegalStateException e) {
            ENABLED = false;
        }
    }

    /**
     * Shuts down the server - Async thread is closed with shutdown boolean.
     */
    public void stop() {
        Log.info(Phrase.WEBSERVER_CLOSE + "");
        shutdown = true;
        try {
            server.close();
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    /**
     * @return DataRequestHandler used by the WebServer.
     */
    public DataRequestHandler getDataReqHandler() {
        return dataReqHandler;
    }
}
