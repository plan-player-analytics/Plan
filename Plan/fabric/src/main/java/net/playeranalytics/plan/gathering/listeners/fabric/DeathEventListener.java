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
package net.playeranalytics.plan.gathering.listeners.fabric;

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
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.playeranalytics.plan.gathering.listeners.FabricListener;
import net.playeranalytics.plan.gathering.listeners.events.PlanFabricEvents;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class DeathEventListener implements FabricListener {

    private final Processing processing;
    private final ErrorLogger errorLogger;
    private final ServerInfo serverInfo;
    private final ExplosionCache explosionCache;

    private boolean isEnabled = false;
    private boolean wasRegistered = false;

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

    @Override
    public void register() {
        if (this.wasRegistered) {
            return;
        }

        PlanFabricEvents.ON_BLOCK_INTERACT.register((player, level, hitResult) -> {
            if (!this.isEnabled) return;

            Block block = level.getBlockState(hitResult.getBlockPos()).getBlock();
            if (block instanceof BedBlock || block instanceof RespawnAnchorBlock) {
                explosionCache.store(new ExplosionMetadata(
                        BuiltInRegistries.BLOCK.getKey(block).getPath().toUpperCase(),
                        player.getUUID(),
                        level.dimension().toString(),
                        hitResult.getBlockPos().getX(),
                        hitResult.getBlockPos().getY(),
                        hitResult.getBlockPos().getZ(),
                        System.currentTimeMillis()
                ));
            }
        });

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, killer, killedEntity, damageSource) ->
                {
                    if (!this.isEnabled) {
                        return;
                    }
                    Entity directCause = Objects.requireNonNullElse(damageSource.getDirectEntity(), killer);
                    PlanFabricEvents.ON_KILLED.invoker().onKilled(killedEntity, directCause, damageSource);
                }
        );

        PlanFabricEvents.ON_KILLED.register((victim, directCause, damageSource) -> {
            if (!this.isEnabled) {
                return;
            }
            long time = System.currentTimeMillis();
            if (victim instanceof ServerPlayer) {
                // Process Death
                SessionCache.getCachedSession(victim.getUUID()).ifPresent(ActiveSession::addDeath);
            }

            try {
                Optional<ServerPlayer> foundKiller = getCause(directCause, victim, damageSource);
                if (foundKiller.isEmpty()) {
                    return;
                }

                ServerPlayer player = foundKiller.get();

                Runnable processor = victim instanceof ServerPlayer
                        ? new PlayerKillProcessor(getKiller(player), getVictim((ServerPlayer) victim), serverInfo.getServerIdentifier(), findWeapon(directCause, victim), time)
                        : new MobKillProcessor(player.getUUID());
                processing.submitCritical(processor);
            } catch (Exception | NoSuchMethodError e) {
                errorLogger.error(e, ErrorContext.builder().related(getClass(), victim, directCause).build());
            }
        });

        this.enable();
        this.wasRegistered = true;
    }

    private Optional<ExplosionMetadata> getRecentBlockExplosion(LivingEntity victim) {
        return explosionCache.getRecentExplosion(
                victim.level().dimension().toString(),
                victim.getX(),
                victim.getY(),
                victim.getZ(),
                System.currentTimeMillis()
        );
    }

    private Optional<ServerPlayer> getBlockExplosionKiller(LivingEntity victim, DamageSource damageSource) {
        if (damageSource != null && damageSource.is(DamageTypes.BAD_RESPAWN_POINT) && victim.level() instanceof ServerLevel serverLevel) {
            return getRecentBlockExplosion(victim)
                    .map(ExplosionMetadata::playerUUID)
                    .map(serverLevel.getServer().getPlayerList()::getPlayer);
        }
        return Optional.empty();
    }

    private String getBlockExplosionWeapon(LivingEntity victim) {
        return getRecentBlockExplosion(victim)
                .map(ExplosionMetadata::explosionBlockName)
                .map(new ItemNameFormatter()::apply)
                .orElse("Block Explosion");
    }

    private PlayerKill.Killer getKiller(ServerPlayer killer) {
        return new PlayerKill.Killer(killer.getUUID(), killer.getName().getString());
    }

    private PlayerKill.Victim getVictim(ServerPlayer victim) {
        return new PlayerKill.Victim(victim.getUUID(), victim.getName().getString());
    }

    public Optional<ServerPlayer> getCause(Entity killer, LivingEntity victim, DamageSource damageSource) {
        if (killer instanceof ServerPlayer player) return Optional.of(player);
        if (killer instanceof TamableAnimal tamed) return getOwner(tamed);
        if (killer instanceof Projectile projectile) return getShooter(projectile);
        if (killer instanceof PrimedTnt tnt) {
            Entity source = tnt.getOwner();
            if (source instanceof ServerPlayer player) return Optional.of(player);
        }
        if (killer == null) return getBlockExplosionKiller(victim, damageSource);

        return Optional.empty();
    }

    public String findWeapon(Entity killer, LivingEntity victim) {
        if (killer == null) return getBlockExplosionWeapon(victim);
        if (killer instanceof ServerPlayer player) return getItemInHand(player);
        if (killer instanceof PrimedTnt) return "TNT";

        // Projectile, EnderCrystal and all other causes that are not known yet
        return new EntityNameFormatter().apply(killer.getType().getDescription().getString());
    }

    private String getItemInHand(ServerPlayer killer) {
        ItemStack itemInHand = killer.getMainHandItem();
        return itemInHand.getItemName().getString();
    }

    private Optional<ServerPlayer> getShooter(Projectile projectile) {
        Entity source = projectile.getOwner();
        if (source instanceof ServerPlayer player) {
            return Optional.of(player);
        }

        return Optional.empty();
    }

    private Optional<ServerPlayer> getOwner(TamableAnimal tameable) {
        if (!tameable.isTame()) {
            return Optional.empty();
        }

        Entity owner = tameable.getOwner();
        if (owner instanceof ServerPlayer player) {
            return Optional.of(player);
        }

        return Optional.empty();
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public void enable() {
        this.isEnabled = true;
    }

    @Override
    public void disable() {
        this.isEnabled = false;
    }
}