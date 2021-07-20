package net.playeranalytics.plan.gathering.listeners.events.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import net.playeranalytics.plan.gathering.listeners.events.PlanFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "checkCanJoin", at = @At(value = "TAIL"))
    public void onLogin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
        PlanFabricEvents.ON_LOGIN.invoker().onLogin(address, profile, cir.getReturnValue());
    }

}
