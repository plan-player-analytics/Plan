package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.player.MobKillProcessor;
import com.djrapitops.plan.system.processing.processors.player.PlayerKillProcessor;
import com.djrapitops.plan.utilities.formatting.EntityNameFormatter;
import com.djrapitops.plan.utilities.formatting.ItemNameFormatter;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Event Listener for EntityDeathEvents.
 *
 * @author Rsl1122
 */
public class DeathEventListener implements Listener {

    private final Processing processing;
    private final ErrorHandler errorHandler;

    @Inject
    public DeathEventListener(
            Processing processing,
            ErrorHandler errorHandler
    ) {
        this.processing = processing;
        this.errorHandler = errorHandler;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        long time = System.currentTimeMillis();
        LivingEntity dead = event.getEntity();

        if (dead instanceof Player) {
            // Process Death
            SessionCache.getCachedSession(dead.getUniqueId()).ifPresent(Session::died);
        }

        try {
            EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
            if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
                return;
            }

            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
            Entity killerEntity = entityDamageByEntityEvent.getDamager();

            UUID uuid = dead instanceof Player ? dead.getUniqueId() : null;
            handleKill(time, uuid, killerEntity);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void handleKill(long time, UUID victimUUID, Entity killerEntity) {
        Runnable processor = null;
        if (killerEntity instanceof Player) {
            processor = handlePlayerKill(time, victimUUID, (Player) killerEntity);
        } else if (killerEntity instanceof Tameable) {
            processor = handlePetKill(time, victimUUID, (Tameable) killerEntity);
        } else if (killerEntity instanceof Projectile) {
            processor = handleProjectileKill(time, victimUUID, (Projectile) killerEntity);
        }
        if (processor != null) {
            processing.submit(processor);
        }
    }

    private Runnable handlePlayerKill(long time, UUID victimUUID, Player killer) {
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

        String weaponName = new ItemNameFormatter().apply(itemInHand.name());

        return victimUUID != null
                ? new PlayerKillProcessor(killer.getUniqueId(), time, victimUUID, weaponName)
                : new MobKillProcessor(killer.getUniqueId());
    }

    private Runnable handlePetKill(long time, UUID victimUUID, Tameable tameable) {
        if (!tameable.isTamed()) {
            return null;
        }

        AnimalTamer owner = tameable.getOwner();
        if (!(owner instanceof Player)) {
            return null;
        }

        String name;
        try {
            name = tameable.getType().name();
        } catch (NoSuchMethodError oldVersionNoTypesError) {
            // getType introduced in 1.9
            name = tameable.getClass().getSimpleName();
        }

        return victimUUID != null
                ? new PlayerKillProcessor(owner.getUniqueId(), time, victimUUID, new EntityNameFormatter().apply(name))
                : new MobKillProcessor(owner.getUniqueId());
    }

    private Runnable handleProjectileKill(long time, UUID victimUUID, Projectile projectile) {
        ProjectileSource source = projectile.getShooter();
        if (!(source instanceof Player)) {
            return null;
        }

        Player player = (Player) source;
        String projectileName = new EntityNameFormatter().apply(projectile.getType().name());

        return victimUUID != null
                ? new PlayerKillProcessor(player.getUniqueId(), time, victimUUID, projectileName)
                : new MobKillProcessor(player.getUniqueId());
    }
}

