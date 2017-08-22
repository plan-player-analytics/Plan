package main.java.com.djrapitops.plan.data.listeners;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.handling.info.KillInfo;
import main.java.com.djrapitops.plan.data.handling.player.DeathProcessor;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.UUID;

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
            plugin.addToProcessQueue(new DeathProcessor(dead.getUniqueId()));
        }

        EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
        if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
        Entity killerEntity = entityDamageByEntityEvent.getDamager();

        UUID killerUUID = null;
        String weapon = null;

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

            killerUUID = killer.getUniqueId();
            weapon = normalizeMaterialName(itemInHand);
        } else if (killerEntity instanceof Wolf) {
            Wolf wolf = (Wolf) killerEntity;
            if (!wolf.isTamed()) {
                return;
            }

            AnimalTamer owner = wolf.getOwner();
            if (owner instanceof Player) {
                killerUUID = owner.getUniqueId();
                weapon = "Wolf";
            }
        } else if (killerEntity instanceof Arrow) {
            Arrow arrow = (Arrow) killerEntity;
            ProjectileSource source = arrow.getShooter();

            if (source instanceof Player) {
                Player player = (Player) source;
                killerUUID = player.getUniqueId();
                weapon = "Bow";
            }
        }

        if (Verify.notNull(killerUUID, weapon)) {

            plugin.addToProcessQueue(new KillInfo(killerUUID, time, dead, weapon));
        }
    }

    /**
     * Normalizes a material name
     *
     * @param material The material
     * @return The normalized material name
     */
    private String normalizeMaterialName(Material material) {
        return WordUtils.capitalizeFully(material.name(), '_').replace('_', ' ');
    }
}

