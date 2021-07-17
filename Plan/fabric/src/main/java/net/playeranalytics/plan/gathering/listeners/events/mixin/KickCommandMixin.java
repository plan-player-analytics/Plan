package net.playeranalytics.plan.gathering.listeners.events.mixin;

import net.minecraft.server.command.KickCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.playeranalytics.plan.gathering.listeners.events.PlanFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(KickCommand.class)
public class KickCommandMixin {

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;disconnect(Lnet/minecraft/text/Text;)V"))
    private static void onKickPlayer(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Text reason, CallbackInfoReturnable<Integer> cir) {
        PlanFabricEvents.ON_KICKED.invoker().onKicked(source, targets, reason);
    }

}
