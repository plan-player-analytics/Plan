package com.djrapitops.plan.sponge.listeners.sponge;

import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.player.NameProcessor;
import com.djrapitops.plugin.api.utility.log.Log;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.UUID;

/**
 * Listener that keeps track of player display name.
 *
 * @author Rsl1122
 */
public class SpongeChatListener {

    @Listener(order = Order.POST)
    public void onPlayerChat(MessageChannelEvent.Chat event, @First Player player) {
        if (event.isCancelled()) {
            return;
        }

        try {
            actOnChatEvent(player);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void actOnChatEvent(@First Player player) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        String displayName = player.getDisplayNameData().displayName().get().toPlain();
        Processing.submit(new NameProcessor(uuid, name, displayName));
    }

}