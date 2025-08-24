/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.gathering.listeners.bukkit;

import com.djrapitops.plan.delivery.formatting.EntityNameFormatter;
import com.djrapitops.plan.delivery.formatting.ItemNameFormatter;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.processing.processors.player.MobKillProcessor;
import com.djrapitops.plan.processing.processors.player.PlayerKillProcessor;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
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
import java.util.Optional;

/**
 * Event Listener for EntityDeathEvents.
 *
 * @author AuroraLS3
 */
public class DeathEventListener implements Listener {

    private final ServerInfo serverInfo;
    private final Processing processing;
    private final ErrorLogger errorLogger;

    @Inject
    public DeathEventListener(
            ServerInfo serverInfo,
            Processing processing,
            ErrorLogger errorLogger
    ) {
        this.serverInfo = serverInfo;
        this.processing = processing;
        this.errorLogger = errorLogger;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        long time = System.currentTimeMillis();
        LivingEntity dead = event.getEntity();

        if (dead instanceof Player) {
            // Process Death
            SessionCache.getCachedSession(dead.getUniqueId()).ifPresent(ActiveSession::addDeath);
        }

        try {
            Optional<Player> foundKiller = findKiller(dead);
            if (foundKiller.isEmpty()) {
                return;
            }
            Player killer = foundKiller.get();

            Runnable processor = dead instanceof Player
                    ? new PlayerKillProcessor(getKiller(killer), getVictim((Player) dead), serverInfo.getServerIdentifier(), findWeapon(dead), time)
                    : new MobKillProcessor(killer.getUniqueId());
            processing.submitCritical(processor);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event, dead).build());
        }
    }

    private PlayerKill.Killer getKiller(Player killer) {
        return new PlayerKill.Killer(killer.getUniqueId(), killer.getName());
    }

    private PlayerKill.Victim getVictim(Player victim) {
        return new PlayerKill.Victim(victim.getUniqueId(), victim.getName(), victim.getFirstPlayed());
    }

    public Optional<Player> findKiller(Entity dead) {
        EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
        if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
            // Not damaged by entity, can't be a player
            return Optional.empty();
        }

        Entity killer = ((EntityDamageByEntityEvent) entityDamageEvent).getDamager();
        if (killer instanceof Player) return Optional.of((Player) killer);
        if (killer instanceof Tameable) return getOwner((Tameable) killer);
        if (killer instanceof Projectile) return getShooter((Projectile) killer);
        if (killer instanceof EnderCrystal) return findKiller(killer); // Recursive call

        return Optional.empty();
    }

    public String findWeapon(Entity dead) {
        EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
        if (entityDamageEvent == null) return "Unknown (No damage cause defined)";
        Entity killer = ((EntityDamageByEntityEvent) entityDamageEvent).getDamager();
        if (killer instanceof Player) return getItemInHand((Player) killer);
        if (killer instanceof Tameable) return getPetType((Tameable) killer);

        // Projectile, EnderCrystal and all other causes that are not known yet
        return new EntityNameFormatter().apply(killer.getType().name());
    }

    private String getPetType(Tameable tameable) {
        try {
            return tameable.getType().name();
        } catch (NoSuchMethodError oldVersionNoTypesError) {
            // getType introduced in 1.9
            return tameable.getClass().getSimpleName();
        }
    }

    private String getItemInHand(Player killer) {
        Material itemInHand;
        try {
            itemInHand = killer.getInventory().getItemInMainHand().getType();
        } catch (NoSuchMethodError oldVersion) {
            try {
                itemInHand = killer.getInventory().getItemInHand().getType(); // Support for non dual wielding versions.
            } catch (Exception | NoSuchMethodError | NoSuchFieldError unknownError) {
                itemInHand = Material.AIR;
            }
        }

        return new ItemNameFormatter().apply(itemInHand.name());
    }

    private Optional<Player> getShooter(Projectile projectile) {
        ProjectileSource source = projectile.getShooter();
        if (source instanceof Player) {
            return Optional.of((Player) source);
        }

        return Optional.empty();
    }

    private Optional<Player> getOwner(Tameable tameable) {
        if (!tameable.isTamed()) {
            return Optional.empty();
        }

        AnimalTamer owner = tameable.getOwner();
        if (owner instanceof Player) {
            return Optional.of((Player) owner);
        }

        return Optional.empty();
    }
}

