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
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class PlanFabricEvents {

    public static final Event<OnKilled> ON_KILLED = EventFactory.createArrayBacked(OnKilled.class, callbacks -> (killed, killer) -> {
        for (OnKilled callback : callbacks) {
            callback.onKilled(killed, killer);
        }
    });

    public static final Event<OnChat> ON_CHAT = EventFactory.createArrayBacked(OnChat.class, callbacks -> (handler, message) -> {
        for (OnChat callback : callbacks) {
            callback.onChat(handler, message);
        }
    });

    public static final Event<OnMove> ON_MOVE = EventFactory.createArrayBacked(OnMove.class, callbacks -> (handler, packet) -> {
        for (OnMove callback : callbacks) {
            callback.onMove(handler, packet);
        }
    });

    public static final Event<OnGameModeChange> ON_GAMEMODE_CHANGE = EventFactory.createArrayBacked(OnGameModeChange.class, callbacks -> (handler, packet) -> {
        for (OnGameModeChange callback : callbacks) {
            callback.onGameModeChange(handler, packet);
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
    public interface OnChat {
        /**
         * Called when a player sends a chat message / command
         *
         * @param handler the handler of the sending player
         * @param message the message sent (starts with "/" if it is a command)
         */
        void onChat(ServerPlayNetworkHandler handler, String message);
    }

    @FunctionalInterface
    public interface OnMove {
        /**
         * Called when a sends a valid movement packet
         *
         * @param handler the handler of the sending player
         * @param packet the send packet
         */
        void onMove(ServerPlayNetworkHandler handler, PlayerMoveC2SPacket packet);
    }

    @FunctionalInterface
    public interface OnGameModeChange {
        /**
         * Called when a player changes gamemode
         *
         * @param player the player that changed gamemodes
         * @param newGameMode the new gamemode
         */
        void onGameModeChange(ServerPlayerEntity player, GameMode newGameMode);
    }

}
