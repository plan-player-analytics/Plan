package main.java.com.djrapitops.plan.systems.listeners;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.systems.processing.player.NameProcessor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Event Listener for AsyncPlayerChatEvents.
 *
 * @author Rsl1122
 */
public class PlanChatListener implements Listener {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanChatListener(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     * ChatEvent listener.
     *
     * @param event Fired Event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player p = event.getPlayer();
        // TODO NameCache to DataCache
        plugin.addToProcessQueue(new NameProcessor(p.getUniqueId(), p.getName(), p.getDisplayName()));
    }
}
