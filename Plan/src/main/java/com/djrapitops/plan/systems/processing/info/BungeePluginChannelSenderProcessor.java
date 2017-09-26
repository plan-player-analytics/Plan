/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing.info;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.systems.processing.Processor;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class BungeePluginChannelSenderProcessor extends Processor<Player> {

    public BungeePluginChannelSenderProcessor(Player object) {
        super(object);
    }

    @Override
    public void process() {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (DataOutputStream out = new DataOutputStream(b)) {
                out.writeUTF("bungee_address_get");
                object.sendPluginMessage(Plan.getInstance(), "BungeeCord", b.toByteArray());
            }
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}