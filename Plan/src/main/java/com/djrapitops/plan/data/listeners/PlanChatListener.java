package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handlers.DemographicsHandler;
import main.java.com.djrapitops.plan.data.UserData;
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
    private final DataCacheHandler handler;
    private final DemographicsHandler demographicsHandler;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanChatListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
        demographicsHandler = handler.getDemographicsHandler();
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
        UserData data = handler.getCurrentData(p.getUniqueId());
        String nickname = p.getDisplayName();
        if (!nickname.isEmpty()) {
            data.addNickname(nickname);
        }
        demographicsHandler.handleChatEvent(event, data);
    }
}
