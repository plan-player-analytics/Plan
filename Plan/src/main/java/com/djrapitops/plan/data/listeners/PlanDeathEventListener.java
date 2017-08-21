package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.handling.KillHandling;
import main.java.com.djrapitops.plan.data.handling.info.DeathInfo;
import main.java.com.djrapitops.plan.data.handling.info.KillInfo;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Event Listener for EntityDeathEvents.
 *
 * @author Rsl1122
 */
public class PlanDeathEventListener implements Listener {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanDeathEventListener(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     * Command use listener.
     *
     * @param event Fired event.
     */
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        long time = MiscUtils.getTime();
        LivingEntity dead = event.getEntity();

        if (dead instanceof Player) {
            plugin.addToProcessQueue(new DeathInfo(dead.getUniqueId()));
        }

        EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
        if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
        Entity killerEntity = entityDamageByEntityEvent.getDamager();

        if (killerEntity instanceof Player) {
            Player killer = (Player) killerEntity;
            Material itemInHand;
            try {
                itemInHand = killer.getInventory().getItemInMainHand().getType();
            } catch (NoSuchMethodError e) {
                try {
                    itemInHand = killer.getInventory().getItemInHand().getType(); // Support for non dual wielding versions.
                } catch (Exception | NoSuchMethodError | NoSuchFieldError e2) {
                    itemInHand = Material.AIR;
                }
            }

            plugin.addToProcessQueue(new KillInfo(killer.getUniqueId(), time, dead, KillHandling.normalizeMaterialName(itemInHand)));
            return;
        }

        if (killerEntity instanceof Wolf) {
            Wolf wolf = (Wolf) killerEntity;

            if (!wolf.isTamed()) {
                return;
            }

            AnimalTamer owner = wolf.getOwner();

            if (!(owner instanceof Player)) {
                return;
            }

            plugin.addToProcessQueue(new KillInfo(owner.getUniqueId(), time, dead, "Wolf"));
        }

        if (killerEntity instanceof Arrow) {
            Arrow arrow = (Arrow) killerEntity;

            ProjectileSource source = arrow.getShooter();

            if (!(source instanceof Player)) {
                return;
            }

            Player player = (Player) source;

            handler.addToPool(new KillInfo(player.getUniqueId(), time, dead, "Bow"));
        }
    }
}

