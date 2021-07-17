package net.playeranalytics.plan.gathering.listeners.events.mixin;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.playeranalytics.plan.gathering.listeners.events.PlanFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Inject(method = "onGameMessage", at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"))
    public void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        PlanFabricEvents.ON_CHAT.invoker().onChat((ServerPlayNetworkHandler) (Object) this, packet.getChatMessage());
    }

    @Inject(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getServerWorld()Lnet/minecraft/server/world/ServerWorld;"))
    public void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        PlanFabricEvents.ON_MOVE.invoker().onMove((ServerPlayNetworkHandler) (Object) this, packet);
    }

}
