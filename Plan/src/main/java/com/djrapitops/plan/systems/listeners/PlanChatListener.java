package main.java.com.djrapitops.plan.systems.listeners;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.processing.player.NameProcessor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

/**
 * Event Listener for AsyncPlayerChatEvents.
 *
 * @author Rsl1122
 */
public class PlanChatListener implements Listener {

    private final Plan plugin;
    private final DataCache dataCache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanChatListener(Plan plugin) {
        this.plugin = plugin;
        dataCache = plugin.getDataCache();
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
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        String displayName = p.getDisplayName();

        DataCache dataCache = plugin.getDataCache();
        if (dataCache.isFirstSession(uuid)) {
            dataCache.firstSessionMessageSent(uuid);
        }

        plugin.addToProcessQueue(new NameProcessor(uuid, name, displayName));
    }
}
