package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.help.HelpMap;

/**
 * Event Listener for PlayerCommandPreprocessEvents.
 *
 * @author Rsl1122
 */
public class PlanCommandPreprocessListener implements Listener {

    private final Plan plugin;
    private final DataCacheHandler handler;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanCommandPreprocessListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
    }

    /**
     * Command use listener.
     *
     * @param event Fired event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }

        String cmd = event.getMessage().split(" ")[0].toLowerCase();

        if (Settings.DO_NOT_LOG_UNKNOWN_COMMANDS.isTrue()) {
            HelpMap helpMap = plugin.getServer().getHelpMap();
            if (helpMap.getHelpTopic(cmd) == null) {
                Log.debug("Ignored command, command is unknown");
                return;
            }
        }

        Player player = event.getPlayer();

        if (player.hasPermission(Permissions.IGNORE_COMMANDUSE.getPermission())) {
            Log.debug("Ignored command, player had ignore permission.");
            return;
        }
        handler.handleCommand(cmd);
    }
}
