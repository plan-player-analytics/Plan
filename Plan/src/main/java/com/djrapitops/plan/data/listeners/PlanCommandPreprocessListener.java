package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handlers.CommandUseHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 *
 * @author Rsl1122
 */
public class PlanCommandPreprocessListener implements Listener {

    private final Plan plugin;
    private final DataCacheHandler handler;
    private final CommandUseHandler serverH;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanCommandPreprocessListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
        serverH = handler.getServerDataHandler();
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
        if (event.getPlayer().hasPermission("plan.ignore.commanduse")) {
            return;
        }
        serverH.handleCommand(event.getMessage().split(" ")[0]);
    }
}
