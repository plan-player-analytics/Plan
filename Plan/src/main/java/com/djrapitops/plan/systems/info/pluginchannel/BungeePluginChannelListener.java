/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.pluginchannel;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.PlanBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class BungeePluginChannelListener implements Listener {

    private final PlanBungee plugin;

    public BungeePluginChannelListener(PlanBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (!e.getTag().equalsIgnoreCase("BungeeCord")) {
            return;
        }

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()))) {
            String channel = in.readUTF(); // channel we delivered
            if (channel.equals("bungee_address_get")) {
                ServerInfo server = plugin.getProxy().getPlayer(e.getReceiver().toString()).getServer().getInfo();
                sendToBukkit(server);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void sendToBukkit(ServerInfo server) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            String accessKey = plugin.getWebServer().getWebAPI().generateNewAccessKey();

            try (DataOutputStream out = new DataOutputStream(stream)) {
                out.writeUTF("bungee_address");
                out.writeUTF(plugin.getWebServer().getAccessAddress() + "<!>" + accessKey);
            }
            server.sendData("Return", stream.toByteArray());
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

}