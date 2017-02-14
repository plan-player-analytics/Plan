package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handlers.GamemodeTimesHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

/**
 *
 * @author Rsl1122
 */
public class PlanGamemodeChangeListener implements Listener {

    private final Plan plugin;
    private final DataCacheHandler handler;
    private final GamemodeTimesHandler gmTimesH;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanGamemodeChangeListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
        gmTimesH = handler.getGamemodeTimesHandler();
    }

    /**
     * GM Change Event Listener.
     *
     * @param event Fired Event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        UserData data = handler.getCurrentData(p.getUniqueId());
        gmTimesH.handleChangeEvent(event, data);
    }
}
