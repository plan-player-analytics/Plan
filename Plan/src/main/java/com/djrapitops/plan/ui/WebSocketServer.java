package com.djrapitops.plan.ui;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import com.djrapitops.plan.ui.webserver.Request;
import com.djrapitops.plan.ui.webserver.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class WebSocketServer {

    private int PORT;
    private boolean ENABLED = false;
    private ServerSocket server;

    private Plan plugin;
    private InspectCacheHandler inspectHandler;
    private AnalysisCacheHandler analysisHandler;
    private DataRequestHandler dataReqHandler;

    private boolean shutdown;

    public WebSocketServer(Plan plugin) {
        this.plugin = plugin;
        this.inspectHandler = plugin.getInspectCache();
        this.PORT = plugin.getConfig().getInt("WebServer.Port");
        shutdown = false;
        dataReqHandler = new DataRequestHandler(plugin);
    }

    public void initServer() {
        //Server is already enabled stop code
        if (ENABLED) {
            return;
        }
        plugin.log("Initializing Webserver..");
        try {
            //Setup server
            try {
                server = new ServerSocket(PORT, 1, InetAddress.getByName("0.0.0.0"));
            } catch (IOException e) {
                System.exit(1);
            }
            //Run server in seperate thread
            (new BukkitRunnable() {
                @Override
                public void run() {
                    while (!shutdown) {
                        Socket socket;
                        InputStream input;
                        OutputStream output;
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
                        }
                    }
                    this.cancel();
                }
            }).runTaskAsynchronously(plugin);

            ENABLED = true;

            plugin.log("Webserver running on PORT "+server.getLocalPort());
        } catch (Exception e) {
            ENABLED = false;
        }
    }

    public void stop() {
        plugin.log("Shutting down Webserver..");
        shutdown = true;
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
