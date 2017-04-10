package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.handling.InfoPoolProcessor;
import main.java.com.djrapitops.plan.data.handling.info.ChatInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 *
 * @author Rsl1122
 */
public class PlanChatListener implements Listener {

    private final Plan plugin;
    private final InfoPoolProcessor processor;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanChatListener(Plan plugin) {
        this.plugin = plugin;
        processor = plugin.getInfoPoolProcessor();
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
        processor.addToPool(new ChatInfo(p.getUniqueId(), p.getDisplayName(), event.getMessage()));
    }
}
