package com.djrapitops.plan.sponge.listeners.sponge;

import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.CommandProcessor;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;

import java.util.Optional;

/**
 * Listener that keeps track of used commands.
 *
 * @author Rsl1122
 */
public class SpongeCommandListener {

    @Listener(order = Order.POST)
    public void onPlayerCommand(SendCommandEvent event, @First Player player) {
        boolean hasIgnorePermission = player.hasPermission(Permissions.IGNORE_COMMAND_USE.getPermission());
        if (event.isCancelled() || hasIgnorePermission) {
            return;
        }
        try {
            actOnCommandEvent(event);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void actOnCommandEvent(SendCommandEvent event) {
        String commandName = event.getCommand();

        boolean logUnknownCommands = Settings.LOG_UNKNOWN_COMMANDS.isTrue();
        boolean combineCommandAliases = Settings.COMBINE_COMMAND_ALIASES.isTrue();

        if (!logUnknownCommands || combineCommandAliases) {
            Optional<? extends CommandMapping> existingCommand = Sponge.getCommandManager().get(commandName);
            if (!existingCommand.isPresent()) {
                if (!logUnknownCommands) {
                    return;
                }
            } else if (combineCommandAliases) {
                commandName = existingCommand.get().getPrimaryAlias();
            }
        }
        Processing.submit(new CommandProcessor(commandName));
    }

}