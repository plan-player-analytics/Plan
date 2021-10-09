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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.explosive.EndCrystal;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Wolf;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.registry.RegistryTypes;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

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
        Living dead = event.entity();

        if (dead instanceof Player) {
            // Process Death
            SessionCache.getCachedSession(dead.uniqueId()).ifPresent(ActiveSession::addDeath);
        }

        try {
            List<EntityDamageSource> causes = event.cause().allOf(EntityDamageSource.class);
            Optional<Player> foundKiller = findKiller(causes, 0);
            if (foundKiller.isEmpty()) {
                return;
            }
            Player killer = foundKiller.get();

            Runnable processor = dead instanceof Player
                    ? new PlayerKillProcessor(getKiller(killer), getVictim((Player) dead), serverInfo.getServerIdentifier(), findWeapon(event), time)
                    : new MobKillProcessor(killer.uniqueId());
            processing.submitCritical(processor);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event, dead).build());
        }
    }

    private PlayerKill.Killer getKiller(Player killer) {
        return new PlayerKill.Killer(killer.uniqueId(), killer.name());
    }

    private PlayerKill.Victim getVictim(Player victim) {
        return new PlayerKill.Victim(victim.uniqueId(), victim.name());
    }

    public Optional<Player> findKiller(List<EntityDamageSource> causes, int depth) {
        if (causes.isEmpty() || causes.size() < depth) {
            return Optional.empty();
        }

        EntityDamageSource damageSource = causes.get(depth);
        Entity killerEntity = damageSource.source();

        if (killerEntity instanceof Player) return Optional.of((Player) killerEntity);
        if (killerEntity instanceof Wolf) return getOwner((Wolf) killerEntity);
        if (killerEntity instanceof Projectile) return getShooter((Projectile) killerEntity);
        if (killerEntity instanceof EndCrystal) return findKiller(causes, depth + 1);
        return Optional.empty();
    }

    public String findWeapon(DestructEntityEvent.Death death) {
        Optional<EntityDamageSource> damagedBy = death.cause().first(EntityDamageSource.class);
        if (damagedBy.isPresent()) {
            EntityDamageSource damageSource = damagedBy.get();
            Entity killerEntity = damageSource.source();

            if (killerEntity instanceof Player) return getItemInHand((Player) killerEntity);
            if (killerEntity instanceof Wolf) return "Wolf";

            Optional<ResourceKey> entityType = killerEntity.type().findKey(RegistryTypes.ENTITY_TYPE);
            if (entityType.isPresent()) {
                // TODO(vankka): check that this is the right thing
                return new EntityNameFormatter().apply(entityType.get().asString());
            }
        }
        return "Unknown";
    }

    private String getItemInHand(Player killer) {
        ItemStack inMainHand = killer.itemInHand(HandTypes.MAIN_HAND);
        ItemStack inHand = inMainHand.isEmpty() ? killer.itemInHand(HandTypes.OFF_HAND) : inMainHand;
        ItemType type = inHand.isEmpty() ? ItemTypes.AIR.get() : inHand.type();
        return new ItemNameFormatter().apply(type.key(RegistryTypes.ITEM_TYPE).asString()); // TODO(vankka): check that this is the right thing
    }

    private Optional<Player> getShooter(Projectile projectile) {
        ProjectileSource source = projectile.shooter().map(Value::get).orElse(null);
        if (source instanceof Player) {
            return Optional.of((Player) source);
        }

        return Optional.empty();
    }

    private Optional<Player> getOwner(Wolf wolf) {
        return wolf.tamer().flatMap(uuid -> Sponge.game().server().player(uuid.get()));
    }
}
