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
import com.djrapitops.plan.gathering.cache.ExplosionCache;
import com.djrapitops.plan.gathering.cache.ExplosionMetadata;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.processing.processors.player.MobKillProcessor;
import com.djrapitops.plan.processing.processors.player.PlayerKillProcessor;
import com.djrapitops.plan.utilities.java.ThrowableUtils;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.projectiles.ProjectileSource;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

/**
 * Event Listener for EntityDeathEvents.
 *
 * @author AuroraLS3
 */
public class DeathEventListener implements Listener {

    private final ServerInfo serverInfo;
    private final Processing processing;
    private final ErrorLogger errorLogger;
    private final ExplosionCache explosionCache;

    @Inject
    public DeathEventListener(
            ServerInfo serverInfo,
            Processing processing,
            ErrorLogger errorLogger,
            ExplosionCache explosionCache
    ) {
        this.serverInfo = serverInfo;
        this.processing = processing;
        this.errorLogger = errorLogger;
        this.explosionCache = explosionCache;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        long time = System.currentTimeMillis();
        LivingEntity dead = event.getEntity();

        if (dead instanceof Player) {
            // Process Death
            SessionCache.getCachedSession(dead.getUniqueId()).ifPresent(ActiveSession::addDeath);
        }

        if (ThrowableUtils.findMethodCall("setHealth").isPresent()) {
            return; // Death was caused by setHealth call, not another player.
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplodeInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        Material type = block.getType();
        if (isExplodingBlockType(type.name())) {
            // Minecraft removes the block before creating the explosion and passes null as the source entity,
            // so the interacting player and block type must be tracked before the explosion occurs.
            explosionCache.store(new ExplosionMetadata(
                    type.name(),
                    event.getPlayer().getUniqueId(),
                    block.getWorld().getUID().toString(),
                    block.getX(),
                    block.getY(),
                    block.getZ(),
                    System.currentTimeMillis()
            ));
        }
    }

    private boolean isExplodingBlockType(String typeName) {
        // BED_BLOCK is used prior to 1.12, color-prefixed _BED materials in 1.12+, RESPAWN_ANCHOR in 1.16+
        return typeName.endsWith("_BED") || "BED_BLOCK".equals(typeName) || "RESPAWN_ANCHOR".equals(typeName);
    }

    private PlayerKill.Killer getKiller(Player killer) {
        return new PlayerKill.Killer(killer.getUniqueId(), killer.getName());
    }

    private PlayerKill.Victim getVictim(Player victim) {
        return new PlayerKill.Victim(victim.getUniqueId(), victim.getName(), victim.getFirstPlayed());
    }

    public Optional<Player> findKiller(Entity dead) {
        EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
        if (entityDamageEvent instanceof EntityDamageByBlockEvent) {
            return getBlockExplosionKiller(dead, entityDamageEvent);
        }

        if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
            // Not damaged by entity, can't be a player
            return Optional.empty();
        }

        Entity killer = ((EntityDamageByEntityEvent) entityDamageEvent).getDamager();
        if (killer instanceof Player) return Optional.of((Player) killer);
        if (killer instanceof Tameable) return getOwner((Tameable) killer);
        if (killer instanceof Projectile) return getShooter((Projectile) killer);
        if (killer instanceof EnderCrystal) return findKiller(killer); // Recursive call
        if (killer instanceof TNTPrimed tnt) {
            Entity source = tnt.getSource();
            if (source instanceof Player player) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    public String findWeapon(Entity dead) {
        EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
        if (entityDamageEvent == null) return "Unknown (No damage cause defined)";
        if (entityDamageEvent instanceof EntityDamageByBlockEvent) {
            return getBlockExplosionWeapon(dead);
        }

        Entity killer = ((EntityDamageByEntityEvent) entityDamageEvent).getDamager();
        if (killer instanceof Player) return getItemInHand((Player) killer);
        if (killer instanceof Tameable) return getPetType((Tameable) killer);
        if (killer instanceof TNTPrimed) return "TNT";
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

    private Optional<ExplosionMetadata> getRecentBlockExplosion(Entity dead) {
        Location loc = dead.getLocation();
        if (loc.getWorld() == null) return Optional.empty();
        return explosionCache.getRecentExplosion(
                loc.getWorld().getUID().toString(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                System.currentTimeMillis()
        );
    }

    private String getBlockExplosionWeapon(Entity dead) {
        Optional<ExplosionMetadata> explosion = getRecentBlockExplosion(dead);
        if (explosion.isPresent()) {
            return new ItemNameFormatter().apply(explosion.get().explosionBlockName());
        }
        return "Block Explosion";
    }

    private Optional<Player> getBlockExplosionKiller(Entity dead, EntityDamageEvent damageEvent) {
        if (damageEvent.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            Optional<ExplosionMetadata> explosion = getRecentBlockExplosion(dead);
            if (explosion.isPresent()) {
                return Optional.ofNullable(Bukkit.getPlayer(explosion.get().playerUUID()));
            }
        }
        return Optional.empty();
    }
}

