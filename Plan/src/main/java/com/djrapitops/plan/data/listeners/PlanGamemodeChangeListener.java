package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Event Listener for PlayerGameModeChangeEvents.
 *
 * @author Rsl1122
 */
public class PlanGamemodeChangeListener implements Listener {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanGamemodeChangeListener(Plan plugin) {
        this.plugin = plugin;
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
        UUID uuid = p.getUniqueId();
        long time = MiscUtils.getTime();
        String gameMode = event.getNewGameMode().name();
        String worldName = p.getWorld().getName();

        Optional<Session> cachedSession = plugin.getDataCache().getCachedSession(uuid);
        if (cachedSession.isPresent()) {
            Session session = cachedSession.get();
            session.changeState(worldName, gameMode, time);
        }
    }
}
