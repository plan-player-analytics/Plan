package main.java.com.djrapitops.plan.data.listeners;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.cache.DataCacheHandler;
import com.djrapitops.plan.data.handlers.KillHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Rsl1122
 */
public class PlanDeathEventListener implements Listener {

    private final Plan plugin;
    private final DataCacheHandler handler;
    private final KillHandler kH;

    public PlanDeathEventListener(Plan plugin) {
        this.plugin = plugin;
        this.handler = plugin.getHandler();
        this.kH = this.handler.getKillHandler();
    }

    /**
     * Command use listener.
     *
     * @param event Fired event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();
        boolean killerIsPlayer = killer != null;
        UserData killersData = null;
        if (killerIsPlayer) {
            killersData = handler.getCurrentData(killer.getUniqueId());
        }
        if (dead instanceof Player) {
            Player killed = (Player) dead;
            UserData killedsData = handler.getCurrentData(killed.getUniqueId());
            if (killerIsPlayer) {
                String weaponName = killer.getInventory().getItemInMainHand().getType().name();
                kH.handlePlayerKill(killersData, killedsData, weaponName);
            }
            kH.handlePlayerDeath(killedsData);
        } else if (killerIsPlayer) {
            kH.handleMobKill(killersData);
        }
    }
}
