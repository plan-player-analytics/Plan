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
package com.djrapitops.plan.gathering.listeners.sponge;

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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.EnderCrystal;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Wolf;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Listener for Deaths and Kills on Sponge.
 *
 * @author AuroraLS3
 */
public class SpongeDeathListener {

    private final ServerInfo serverInfo;
    private final Processing processing;
    private final ErrorLogger errorLogger;

    @Inject
    public SpongeDeathListener(
            ServerInfo serverInfo,
            Processing processing,
            ErrorLogger errorLogger
    ) {
        this.serverInfo = serverInfo;
        this.processing = processing;
        this.errorLogger = errorLogger;
    }

    @Listener
    public void onEntityDeath(DestructEntityEvent.Death event) {
        long time = System.currentTimeMillis();
        Living dead = event.getTargetEntity();

        if (dead instanceof Player) {
            // Process Death
            SessionCache.getCachedSession(dead.getUniqueId()).ifPresent(ActiveSession::addDeath);
        }

        try {
            List<EntityDamageSource> causes = event.getCause().allOf(EntityDamageSource.class);
            Optional<Player> foundKiller = findKiller(causes, 0);
            if (!foundKiller.isPresent()) {
                return;
            }
            Player killer = foundKiller.get();

            Runnable processor = dead instanceof Player
                    ? new PlayerKillProcessor(getKiller(killer), getVictim((Player) dead), serverInfo.getServerIdentifier(), findWeapon(event), time)
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
        return new PlayerKill.Victim(victim.getUniqueId(), victim.getName());
    }

    public Optional<Player> findKiller(List<EntityDamageSource> causes, int depth) {
        if (causes.isEmpty() || causes.size() < depth) {
            return Optional.empty();
        }

        EntityDamageSource damageSource = causes.get(depth);
        Entity killerEntity = damageSource.getSource();

        if (killerEntity instanceof Player) return Optional.of((Player) killerEntity);
        if (killerEntity instanceof Wolf) return getOwner((Wolf) killerEntity);
        if (killerEntity instanceof Projectile) return getShooter((Projectile) killerEntity);
        if (killerEntity instanceof EnderCrystal) return findKiller(causes, depth + 1);
        return Optional.empty();
    }

    public String findWeapon(DestructEntityEvent.Death death) {
        Optional<EntityDamageSource> damagedBy = death.getCause().first(EntityDamageSource.class);
        if (damagedBy.isPresent()) {
            EntityDamageSource damageSource = damagedBy.get();
            Entity killerEntity = damageSource.getSource();

            if (killerEntity instanceof Player) return getItemInHand((Player) killerEntity);
            if (killerEntity instanceof Wolf) return "Wolf";

            return new EntityNameFormatter().apply(killerEntity.getType().getName());
        }
        return "Unknown";
    }

    private String getItemInHand(Player killer) {
        Optional<ItemStack> inMainHand = killer.getItemInHand(HandTypes.MAIN_HAND);
        ItemStack inHand = inMainHand.orElse(
                killer.getItemInHand(HandTypes.OFF_HAND)
                        .orElse(ItemStack.empty()));
        ItemType type = inHand.isEmpty() ? ItemTypes.AIR : inHand.getType();
        return new ItemNameFormatter().apply(type.getName());
    }

    private Optional<Player> getShooter(Projectile projectile) {
        ProjectileSource source = projectile.getShooter();
        if (source instanceof Player) {
            return Optional.of((Player) source);
        }

        return Optional.empty();
    }

    private Optional<Player> getOwner(Wolf wolf) {
        Optional<Optional<UUID>> isTamed = wolf.get(Keys.TAMED_OWNER);
        if (!isTamed.isPresent()) return Optional.empty();
        Optional<UUID> owner = isTamed.get();

        return owner.flatMap(Sponge.getGame().getServer()::getPlayer);
    }
}