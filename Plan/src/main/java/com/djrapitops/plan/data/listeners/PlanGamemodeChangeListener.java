package main.java.com.djrapitops.plan.data.listeners;

import java.util.Date;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.handling.InfoPoolProcessor;
import main.java.com.djrapitops.plan.data.handling.info.GamemodeInfo;
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
    private final InfoPoolProcessor processor;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanGamemodeChangeListener(Plan plugin) {
        this.plugin = plugin;
        processor = plugin.getInfoPoolProcessor();
        
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
        processor.addToPool(new GamemodeInfo(p.getUniqueId(), new Date().getTime(), event.getNewGameMode()));
    }
}
