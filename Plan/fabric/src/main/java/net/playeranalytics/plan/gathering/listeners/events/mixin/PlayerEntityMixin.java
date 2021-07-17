package net.playeranalytics.plan.gathering.listeners.events.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.playeranalytics.plan.gathering.listeners.events.PlanFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "onKilledOther", at = @At(value = "TAIL"))
    public void onDeath(ServerWorld world, LivingEntity other, CallbackInfo ci) {
        PlanFabricEvents.ON_KILLED.invoker().onKilled(other, (PlayerEntity) (Object) this);
    }

}
