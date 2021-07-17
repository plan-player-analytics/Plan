package net.playeranalytics.plan.gathering.listeners.fabric;

import com.djrapitops.plan.delivery.formatting.EntityNameFormatter;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.processing.processors.player.MobKillProcessor;
import com.djrapitops.plan.processing.processors.player.PlayerKillProcessor;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.playeranalytics.plan.gathering.listeners.FabricListener;
import net.playeranalytics.plan.gathering.listeners.events.PlanFabricEvents;

import javax.inject.Inject;
import java.util.Optional;

public class DeathEventListener implements FabricListener {

    private final Processing processing;
    private final ErrorLogger errorLogger;

    private boolean isEnabled = false;

    @Inject
    public DeathEventListener(
            Processing processing,
            ErrorLogger errorLogger
    ) {
        this.processing = processing;
        this.errorLogger = errorLogger;
    }

    @Override
    public void register() {
        PlanFabricEvents.ON_KILLED.register((dead, killer) -> {
            if (!this.isEnabled) {
                return;
            }
            long time = System.currentTimeMillis();
            if (dead instanceof ServerPlayerEntity) {
                // Process Death
                SessionCache.getCachedSession(dead.getUuid()).ifPresent(ActiveSession::addDeath);
            }

            try {
                Optional<ServerPlayerEntity> foundKiller = getCause(killer);
                if (foundKiller.isEmpty()) {
                    return;
                }

                ServerPlayerEntity player = foundKiller.get();

                Runnable processor = dead instanceof ServerPlayerEntity
                        ? new PlayerKillProcessor(player.getUuid(), time, dead.getUuid(), findWeapon(player))
                        : new MobKillProcessor(player.getUuid());
                processing.submitCritical(processor);
            } catch (Exception e) {
                errorLogger.error(e, ErrorContext.builder().related(getClass(), dead, killer).build());
            }

        });
        this.enable();
    }

    public Optional<ServerPlayerEntity> getCause(Entity killer) {
        if (killer instanceof ServerPlayerEntity) return Optional.of((ServerPlayerEntity) killer);
        if (killer instanceof TameableEntity) return getOwner((TameableEntity) killer);
        if (killer instanceof ProjectileEntity) return getShooter((ProjectileEntity) killer);
        return Optional.empty();
    }

    public String findWeapon(Entity killer) {
        if (killer instanceof ServerPlayerEntity) return getItemInHand((ServerPlayerEntity) killer);

        // Projectile, EnderCrystal and all other causes that are not known yet
        return new EntityNameFormatter().apply(killer.getType().getName().asString());
    }

    private String getItemInHand(ServerPlayerEntity killer) {
        ItemStack itemInHand = killer.getMainHandStack();
        return itemInHand.getItem().getName().asString();
    }

    private Optional<ServerPlayerEntity> getShooter(ProjectileEntity projectile) {
        Entity source = projectile.getOwner();
        if (source instanceof ServerPlayerEntity) {
            return Optional.of((ServerPlayerEntity) source);
        }

        return Optional.empty();
    }

    private Optional<ServerPlayerEntity> getOwner(TameableEntity tameable) {
        if (!tameable.isTamed()) {
            return Optional.empty();
        }

        Entity owner = tameable.getOwner();
        if (owner instanceof ServerPlayerEntity) {
            return Optional.of((ServerPlayerEntity) owner);
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
