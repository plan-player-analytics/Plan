package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

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
        if (Permissions.IGNORE_COMMANDUSE.userHasThisPermission(event.getPlayer())) {
            return;
        }
        handler.handleCommand(event.getMessage().split(" ")[0]);
    }
}
