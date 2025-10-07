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
package net.playeranalytics.plan.gathering.listeners.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import net.playeranalytics.plan.PlanFabric;
import net.playeranalytics.plan.gathering.FabricPlayerPositionTracker;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.UUID;

public class PlanFabricEvents {

    public static final Event<OnKilled> ON_KILLED = EventFactory.createArrayBacked(OnKilled.class, callbacks -> (killed, killer) -> {
        for (OnKilled callback : callbacks) {
            callback.onKilled(killed, killer);
        }
    });

    public static final Event<OnCommand> ON_COMMAND = EventFactory.createArrayBacked(OnCommand.class, callbacks -> (handler, command) -> {
        for (OnCommand callback : callbacks) {
            callback.onCommand(handler, command);
        }
    });

    public static final Event<OnMove> ON_MOVE = EventFactory.createArrayBacked(OnMove.class, callbacks -> (handler, packet) -> {
        for (OnMove callback : callbacks) {
            UUID playerUUID = handler.player.getUuid();
            double[] position = FabricPlayerPositionTracker.getPosition(playerUUID);
            double x = position[0];
            double y = position[1];
            double z = position[2];
            float yaw = (float) position[3];
            float pitch = (float) position[4];
            if (FabricPlayerPositionTracker.moved(playerUUID, packet.getX(x), packet.getY(y), packet.getZ(z), packet.getYaw(yaw), packet.getPitch(pitch))) {
                callback.onMove(handler, packet);
            }
        }
    });

    public static final Event<OnGameModeChange> ON_GAMEMODE_CHANGE = EventFactory.createArrayBacked(OnGameModeChange.class, callbacks -> (handler, packet) -> {
        for (OnGameModeChange callback : callbacks) {
            callback.onGameModeChange(handler, packet);
        }
    });

    public static final Event<OnPlayerKicked> ON_KICKED = EventFactory.createArrayBacked(OnPlayerKicked.class, callbacks -> (source, targets, reason) -> {
        for (OnPlayerKicked callback : callbacks) {
            callback.onKicked(source, targets, reason);
        }
    });

    public static final Event<OnLogin> ON_LOGIN = EventFactory.createArrayBacked(OnLogin.class, callbacks -> (address, profile, reason) -> {
        for (OnLogin callback : callbacks) {
            callback.onLogin(address, profile, reason);
        }
    });

    public static final Event<OnClientHandshake> ON_HANDSHAKE = EventFactory.createArrayBacked(OnClientHandshake.class, callbacks -> packet -> {
        for (OnClientHandshake callback : callbacks) {
            callback.onHandshake(packet);
        }
    });

    /**
     * Called when Plan is enabled.
     * <p>
     * This includes, but might not be limited to:
     * <ul>
     * <li>First time the plugin enables successfully</li>
     * <li>Plan is reloaded</li>
     * <li>Plan is enabled after it was disabled</li>
     * </ul>
     * <p>
     * This event provides full access to the Plan instance. However, <strong>it is advised to
     * only call {@link PlanFabric#isSystemEnabled} to determine if the enable was successful.</strong>
     * It is not guaranteed that this event is called when the plugin fails to enable properly.
     */
    public static final Event<OnEnable> ON_ENABLE = EventFactory.createArrayBacked(OnEnable.class, callbacks -> plugin -> {
        for (OnEnable callback : callbacks) {
            callback.onEnable(plugin);
        }
    });

    @FunctionalInterface
    public interface OnKilled {
        /**
         * Called when a living entity is killed
         *
         * @param killed the entity that died
         * @param killer the entity that killed
         */
        void onKilled(LivingEntity killed, Entity killer);
    }

    @FunctionalInterface
    public interface OnCommand {
        /**
         * Called when a player sends a chat message / command
         *
         * @param handler the handler of the sending player
         * @param message the message sent (starts with "/" if it is a command)
         */
        void onCommand(ServerPlayNetworkHandler handler, String message);
    }

    @FunctionalInterface
    public interface OnMove {
        /**
         * Called when a sends a valid movement packet
         *
         * @param handler the handler of the sending player
         * @param packet  the send packet
         */
        void onMove(ServerPlayNetworkHandler handler, PlayerMoveC2SPacket packet);
    }

    @FunctionalInterface
    public interface OnGameModeChange {
        /**
         * Called when a player changes gamemode
         *
         * @param player      the player that changed gamemodes
         * @param newGameMode the new gamemode
         */
        void onGameModeChange(ServerPlayerEntity player, GameMode newGameMode);
    }

    @FunctionalInterface
    public interface OnPlayerKicked {
        /**
         * Called when a player (or multiple) get kicked from the server
         *
         * @param source  the source that initated the kick
         * @param targets the player(s) that got kicked
         * @param reason  the provided kick reason
         */
        void onKicked(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Text reason);
    }

    @FunctionalInterface
    public interface OnLogin {
        /**
         * Called when a player attempts to login
         *
         * @param address the address of the player
         * @param profile the profile of the player
         * @param reason  the provided kick reason (null if player is permitted to join)
         */
        void onLogin(SocketAddress address, PlayerConfigEntry profile, Text reason);
    }

    @FunctionalInterface
    public interface OnClientHandshake {
        /**
         * Called when a player attempts to login
         *
         * @param packet Handshake packet
         */
        void onHandshake(HandshakeC2SPacket packet);
    }

    @FunctionalInterface
    public interface OnEnable {
        void onEnable(PlanFabric plugin);
    }
}
