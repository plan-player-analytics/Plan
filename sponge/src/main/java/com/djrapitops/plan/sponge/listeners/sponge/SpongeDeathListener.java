package com.djrapitops.plan.sponge.listeners.sponge;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.player.SpongeKillProcessor;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Format;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
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

import java.util.Optional;
import java.util.UUID;

/**
 * Listener for Deaths and Kills on Sponge.
 *
 * @author Rsl1122
 */
public class SpongeDeathListener {

    @Listener
    public void onEntityDeath(DestructEntityEvent.Death event) {
        long time = System.currentTimeMillis();
        Living dead = event.getTargetEntity();

        if (dead instanceof Player) {
            // Process Death
            Processing.submitCritical(() -> SessionCache.getCachedSession(dead.getUniqueId()).ifPresent(Session::died));
        }

        try {
            Optional<EntityDamageSource> optDamageSource = event.getCause().first(EntityDamageSource.class);
            if (optDamageSource.isPresent()) {
                EntityDamageSource damageSource = optDamageSource.get();
                Entity killerEntity = damageSource.getSource();
                handleKill(time, dead, killerEntity);
            }
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void handleKill(long time, Living dead, Entity killerEntity) {
        SpongeKillProcessor processor = null;
        if (killerEntity instanceof Player) {
            processor = handlePlayerKill(time, dead, (Player) killerEntity);
        } else if (killerEntity instanceof Wolf) {
            processor = handleWolfKill(time, dead, (Wolf) killerEntity);
        } else if (killerEntity instanceof Projectile) {
            processor = handleProjectileKill(time, dead, (Projectile) killerEntity);
        }
        if (processor != null) {
            Processing.submit(processor);
        }
    }

    private SpongeKillProcessor handlePlayerKill(long time, Living dead, Player killer) {

        Optional<ItemStack> inMainHand = killer.getItemInHand(HandTypes.MAIN_HAND);
        ItemStack inHand = inMainHand.orElse(killer.getItemInHand(HandTypes.OFF_HAND).orElse(ItemStack.empty()));
        ItemType type = inHand.isEmpty() ? ItemTypes.AIR : inHand.getType();

        return new SpongeKillProcessor(killer.getUniqueId(), time, getUUID(dead), normalizeItemName(type));
    }

    private UUID getUUID(Living dead) {
        if (dead instanceof Player) {
            return dead.getUniqueId();
        }
        return null;
    }

    private SpongeKillProcessor handleWolfKill(long time, Living dead, Wolf wolf) {
        Optional<Optional<UUID>> owner = wolf.get(Keys.TAMED_OWNER);

        if (!owner.isPresent()) {
            return null;
        }

        return owner.get().map(
                uuid -> new SpongeKillProcessor(uuid, time, getUUID(dead), "Wolf")
        ).orElse(null);
    }

    private SpongeKillProcessor handleProjectileKill(long time, Living dead, Projectile projectile) {
        ProjectileSource source = projectile.getShooter();
        if (!(source instanceof Player)) {
            return null;
        }

        Player player = (Player) source;

        return new SpongeKillProcessor(player.getUniqueId(), time, getUUID(dead),
                new Format(projectile.getType().getName()).capitalize().toString()
        );
    }

    /**
     * Normalizes an item name
     *
     * @param type The type of the item
     * @return The normalized item name
     */
    private String normalizeItemName(ItemType type) {
        String[] parts = type.getName().split("_");
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