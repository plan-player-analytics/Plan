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
package com.djrapitops.plan.gathering.listeners.nukkit;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.passive.EntityTameable;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.item.Item;
import com.djrapitops.plan.delivery.formatting.EntityNameFormatter;
import com.djrapitops.plan.delivery.formatting.ItemNameFormatter;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.processing.processors.player.MobKillProcessor;
import com.djrapitops.plan.processing.processors.player.PlayerKillProcessor;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.L;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Event Listener for detecting player and mob deaths.
 *
 * @author AuroraLS3
 */
public class DeathEventListener implements Listener {

    private final Processing processing;
    private final ErrorLogger errorLogger;

    @Inject
    public DeathEventListener(
            Processing processing,
            ErrorLogger errorLogger
    ) {
        this.processing = processing;
        this.errorLogger = errorLogger;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        long time = System.currentTimeMillis();
        Player dead = event.getEntity();
        SessionCache.getCachedSession(dead.getUniqueId()).ifPresent(Session::died);

        try {
            EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
            if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
                return;
            }

            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
            Entity killerEntity = entityDamageByEntityEvent.getDamager();

            UUID uuid = dead.getUniqueId();
            handleKill(time, uuid, killerEntity);
        } catch (Exception e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder().related(event, dead).build());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMobDeath(EntityDeathEvent event) {
        long time = System.currentTimeMillis();
        Entity dead = event.getEntity();

        try {
            EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
            if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
                return;
            }

            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
            Entity killerEntity = entityDamageByEntityEvent.getDamager();

            handleKill(time, /* Not a player */ null, killerEntity);
        } catch (Exception e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder().related(event, dead).build());
        }
    }

    private void handleKill(long time, UUID victimUUID, Entity killerEntity) {
        Runnable processor = null;
        if (killerEntity instanceof Player) {
            processor = handlePlayerKill(time, victimUUID, (Player) killerEntity);
        } else if (killerEntity instanceof EntityTameable) {
            processor = handlePetKill(time, victimUUID, (EntityTameable) killerEntity);
        } else if (killerEntity instanceof EntityProjectile) {
            processor = handleProjectileKill(time, victimUUID, (EntityProjectile) killerEntity);
        }
        if (processor != null) {
            processing.submit(processor);
        }
    }

    private Runnable handlePlayerKill(long time, UUID victimUUID, Player killer) {
        Item itemInHand = killer.getInventory().getItemInHand();

        String weaponName = new ItemNameFormatter().apply(itemInHand.getName());

        return victimUUID != null
                ? new PlayerKillProcessor(killer.getUniqueId(), time, victimUUID, weaponName)
                : new MobKillProcessor(killer.getUniqueId());
    }

    private Runnable handlePetKill(long time, UUID victimUUID, EntityTameable tameable) {
        if (!tameable.isTamed()) {
            return null;
        }

        Player owner = tameable.getOwner();

        String name;
        try {
            name = tameable.getName();
        } catch (NoSuchMethodError oldVersionNoTypesError) {
            // getType introduced in 1.9
            name = tameable.getClass().getSimpleName();
        }

        return victimUUID != null
                ? new PlayerKillProcessor(owner.getUniqueId(), time, victimUUID, new EntityNameFormatter().apply(name))
                : new MobKillProcessor(owner.getUniqueId());
    }

    private Runnable handleProjectileKill(long time, UUID victimUUID, EntityProjectile projectile) {
        Entity source = projectile.shootingEntity;
        if (!(source instanceof Player)) {
            return null;
        }

        Player player = (Player) source;
        String projectileName = new EntityNameFormatter().apply(projectile.getName());

        return victimUUID != null
                ? new PlayerKillProcessor(player.getUniqueId(), time, victimUUID, projectileName)
                : new MobKillProcessor(player.getUniqueId());
    }
}

