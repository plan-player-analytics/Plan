package com.djrapitops.plan.bukkit.listeners.bukkit;

import com.djrapitops.plan.bukkit.PlanBukkit;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.CommandProcessor;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Event Listener for PlayerCommandPreprocessEvents.
 *
 * @author Rsl1122
 */
public class CommandPreprocessListener implements Listener {

    private final PlanBukkit plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public CommandPreprocessListener(PlanBukkit plugin) {
        this.plugin = plugin;
    }

    /**
     * Command use listener.
     *
     * @param event Fired event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        boolean hasIgnorePermission = event.getPlayer().hasPermission(Permissions.IGNORE_COMMAND_USE.getPermission());
        if (event.isCancelled() || hasIgnorePermission) {
            return;
        }

        try {
            actOnCommandEvent(event);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void actOnCommandEvent(PlayerCommandPreprocessEvent event) {
        String commandName = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        boolean logUnknownCommands = Settings.LOG_UNKNOWN_COMMANDS.isTrue();
        boolean combineCommandAliases = Settings.COMBINE_COMMAND_ALIASES.isTrue();

        if (!logUnknownCommands || combineCommandAliases) {
            Command command = getBukkitCommand(commandName);
            if (command == null) {
                if (!logUnknownCommands) {
                    return;
                }
            } else if (combineCommandAliases) {
                commandName = command.getName();
            }
        }
        Processing.submit(new CommandProcessor(commandName));
    }

    private Command getBukkitCommand(String commandName) {
        Command command = plugin.getServer().getPluginCommand(commandName);
        if (command == null) {
            try {
                command = plugin.getServer().getCommandMap().getCommand(commandName);
            } catch (NoSuchMethodError ignored) {
                /* Ignored, Bukkit 1.8 has no such method */
            }
        }
        return command;
    }
}
