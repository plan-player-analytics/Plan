package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handlers.KillHandler;
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
    private final DataCacheHandler handler;
    private final KillHandler kH;

    /**
     *
     * @param plugin
     */
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
        if (killerIsPlayer) {
            DBCallableProcessor deathProcess = new DBCallableProcessor() {
                @Override
                public void process(UserData killersData) {
                    continueProcessing(dead, killerIsPlayer, killer, killersData);
                }
            };
            handler.getUserDataForProcessing(deathProcess, killer.getUniqueId());
        } else {
            continueProcessing(dead, false, null, null);
        }
    }

    /**
     *
     * @param dead
     * @param killerIsPlayer
     * @param killer
     * @param killersData
     */
    public void continueProcessing(LivingEntity dead, boolean killerIsPlayer, Player killer, UserData killersData) {
        if (dead instanceof Player) {
            Player killed = (Player) dead;
            DBCallableProcessor deathProcess = new DBCallableProcessor() {
                @Override
                public void process(UserData killedsData) {
                    if (killerIsPlayer) {
                        String weaponName = killer.getInventory().getItemInMainHand().getType().name();
                        kH.handlePlayerKill(killersData, killed.getUniqueId(), weaponName);
                    }
                    kH.handlePlayerDeath(killedsData);
                }
            };
            handler.getUserDataForProcessing(deathProcess, killed.getUniqueId());
        } else if (killerIsPlayer) {
            kH.handleMobKill(killersData);
        }
    }
}
