package main.java.com.djrapitops.plan.data.listeners;

import java.util.Date;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.handling.InfoPoolProcessor;
import main.java.com.djrapitops.plan.data.handling.info.DeathInfo;
import main.java.com.djrapitops.plan.data.handling.info.KillInfo;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 *
 * @author Rsl1122
 */
public class PlanDeathEventListener implements Listener {

    private final Plan plugin;
    private final InfoPoolProcessor processor;

    /**
     *
     * @param plugin
     */
    public PlanDeathEventListener(Plan plugin) {
        this.plugin = plugin;
        this.processor = plugin.getInfoPoolProcessor();
    }

    /**
     * Command use listener.
     *
     * @param event Fired event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        long time = new Date().getTime();
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();
        boolean killerIsPlayer = killer != null;
        if (killerIsPlayer) {
            processor.addToPool(new KillInfo(killer.getUniqueId(), time, dead, killer.getInventory().getItemInMainHand().getType().name()));
        }
        if (dead instanceof Player) {
            processor.addToPool(new DeathInfo(((Player) dead).getUniqueId()));
        }
    }
}
