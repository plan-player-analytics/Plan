package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.processing.processors.player.DeathProcessor;
import com.djrapitops.plan.system.processing.processors.player.KillProcessor;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Format;
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
public class DeathEventListener implements Listener {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public DeathEventListener(Plan plugin) {
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

        try {
            EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
            if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
                return;
            }

            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
            Entity killerEntity = entityDamageByEntityEvent.getDamager();

            if (killerEntity instanceof Player) {
                handlePlayerKill(time, dead, (Player) killerEntity);
            } else if (killerEntity instanceof Wolf) {
                handleWolfKill(time, dead, (Wolf) killerEntity);
            } else if (killerEntity instanceof Arrow) {
                handleArrowKill(time, dead, (Arrow) killerEntity);
            }
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void handlePlayerKill(long time, LivingEntity dead, Player killer) {
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

        plugin.addToProcessQueue(new KillProcessor(killer.getUniqueId(), time, dead, normalizeMaterialName(itemInHand)));
    }

    private void handleWolfKill(long time, LivingEntity dead, Wolf wolf) {
        if (!wolf.isTamed()) {
            return;
        }

        AnimalTamer owner = wolf.getOwner();
        if (!(owner instanceof Player)) {
            return;
        }

        plugin.addToProcessQueue(new KillProcessor(owner.getUniqueId(), time, dead, "Wolf"));
    }

    private void handleArrowKill(long time, LivingEntity dead, Arrow arrow) {
        ProjectileSource source = arrow.getShooter();
        if (!(source instanceof Player)) {
            return;
        }

        Player player = (Player) source;

        plugin.addToProcessQueue(new KillProcessor(player.getUniqueId(), time, dead, "Bow"));
    }

    /**
     * Normalizes a material name
     *
     * @param material The material
     * @return The normalized material name
     */
    private String normalizeMaterialName(Material material) {
        String[] parts = material.name().split("_");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = new Format(parts[i]).capitalize().toString();
            builder.append(part);
            if (i < parts.length - 1) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }
}

