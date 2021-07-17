package net.playeranalytics.plan.gathering.listeners.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class PlanFabricEvents {

    public static final Event<OnKilled> ON_KILLED = EventFactory.createArrayBacked(OnKilled.class, callbacks -> (killed, killer) -> {
        for (OnKilled callback : callbacks) {
            callback.onKilled(killed, killer);
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

}
