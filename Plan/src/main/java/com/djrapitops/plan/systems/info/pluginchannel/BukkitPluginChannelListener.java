/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.pluginchannel;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class BukkitPluginChannelListener implements PluginMessageListener {

    private final Plan plugin;

    public BukkitPluginChannelListener(Plan plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String subChannel = in.readUTF();
            String address = in.readUTF();

            if ("bungee_address".equals(subChannel)) {
                plugin.getServerInfoManager().saveBungeeConnectionAddress(address);
                Log.info("-----------------------------------");
                Log.info("Recieved Bungee WebServer address through plugin channel, restarting Plan.");
                Log.info("-----------------------------------");
                plugin.restart();
                notifyAll();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}