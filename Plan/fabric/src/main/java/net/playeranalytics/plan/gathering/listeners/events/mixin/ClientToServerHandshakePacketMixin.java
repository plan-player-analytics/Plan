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
package net.playeranalytics.plan.gathering.listeners.events.mixin;

import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.network.LocalServerHandshakeNetworkHandler;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.playeranalytics.plan.gathering.listeners.events.PlanFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin({ServerHandshakeNetworkHandler.class, LocalServerHandshakeNetworkHandler.class})
public class ClientToServerHandshakePacketMixin {

    @Inject(method = "onHandshake", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerHandshakeNetworkHandler;onHandshake(Lnet/minecraft/network/packet/c2s/handshake/HandshakeC2SPacket;)V"))
    public static void onClientHandshakeFromNetwork(HandshakeC2SPacket packet) {
        PlanFabricEvents.ON_HANDSHAKE.invoker().onHandshake(packet);
    }

    @Inject(method = "onHandshake", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/LocalServerHandshakeNetworkHandler;onHandshake(Lnet/minecraft/network/packet/c2s/handshake/HandshakeC2SPacket;)V"))
    public static void onClienthandshakeFromLocal(HandshakeC2SPacket packet) {
        PlanFabricEvents.ON_HANDSHAKE.invoker().onHandshake(packet);
    }
}
