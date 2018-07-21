package com.djrapitops.plan.sponge.listeners.sponge;

import com.djrapitops.plan.system.afk.AFKTracker;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.api.utility.log.Log;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.UUID;

/**
 * Listener that keeps track of actions that are not considered being AFK.
 * <p>
 * Additional Listener calls in SpongePlayerListener to avoid having HIGHEST priority listeners.
 *
 * @author Rsl1122
 * @see SpongePlayerListener
 */
public class SpongeAFKListener {

    // Static so that /reload does not cause afk tracking to fail.
    public static final AFKTracker AFK_TRACKER = new AFKTracker();

    private void event(TargetPlayerEvent event) {
        try {
            performedAction(event.getTargetEntity());
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    @Listener(order = Order.POST)
    public void onMove(MoveEntityEvent event, @First Player player) {
        performedAction(player);
    }

    @Listener(order = Order.POST)
    public void onPlayerChat(MessageChannelEvent.Chat event, @First Player player) {
        performedAction(player);
    }

    private void performedAction(Player player) {
        UUID uuid = player.getUniqueId();
        long time = System.currentTimeMillis();

        if (player.hasPermission(Permissions.IGNORE_AFK.getPermission())) {
            AFK_TRACKER.hasIgnorePermission(uuid);
        }

        AFK_TRACKER.performedAction(uuid, time);
    }

    @Listener(order = Order.POST)
    public void onPlayerCommand(SendCommandEvent event, @First Player player) {
        performedAction(player);

        boolean isAfkCommand = event.getCommand().toLowerCase().startsWith("afk");
        if (isAfkCommand) {
            AFK_TRACKER.usedAfkCommand(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @Listener(order = Order.POST)
    public void onSettingsChange(PlayerChangeClientSettingsEvent event) {
        event(event);
    }

}