/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.listeners;

import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.systems.processing.player.BungeePlayerRegisterProcessor;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

/**
 * Player Join listener for Bungee.
 *
 * @author Rsl1122
 */
public class BungeePlayerListener implements Listener {

    private final PlanBungee plugin;

    public BungeePlayerListener(PlanBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            String name = player.getName();
            long now = MiscUtils.getTime();

            plugin.getProcessingQueue().addToQueue(new BungeePlayerRegisterProcessor(uuid, name, now));
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }
}