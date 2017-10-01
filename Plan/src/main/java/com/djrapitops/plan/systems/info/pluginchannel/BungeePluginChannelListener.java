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
        String tag = e.getTag();
        Log.debug(tag);
        if (!tag.equalsIgnoreCase("BungeeCord")) {
            return;
        }

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()))) {
            String channel = in.readUTF();
            Log.debug("Received plugin channel message on channel: " + channel);
            if ("bungee_address_get".equals(channel)) {
                ServerInfo server = plugin.getProxy().getPlayer(e.getReceiver().toString()).getServer().getInfo();
                sendToBukkit(server);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void sendToBukkit(ServerInfo server) {
        Log.debug("Sending data to bukkit through plugin channel");
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            String accessKey = plugin.getWebServer().getWebAPI().generateNewAccessKey();

            try (DataOutputStream out = new DataOutputStream(stream)) {
                out.writeUTF("Forward");
                out.writeUTF(server.getName());
                out.writeUTF("Plan");
                out.writeUTF("bungee_address");
                out.writeUTF(plugin.getWebServer().getAccessAddress() + "<!>" + accessKey);
            }
            server.sendData("Return", stream.toByteArray());
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

}