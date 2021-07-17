package net.playeranalytics.plan.gathering.listeners.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;

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

}
