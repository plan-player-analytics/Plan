package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.processing.processors.CommandProcessor;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
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

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public CommandPreprocessListener(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     * Command use listener.
     *
     * @param event Fired event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();

        try {
            if (player.hasPermission(Permissions.IGNORE_COMMANDUSE.getPermission())) {
                return;
            }

            String commandName = event.getMessage().substring(1).split(" ")[0].toLowerCase();

            boolean logUnknownCommands = Settings.LOG_UNKNOWN_COMMANDS.isTrue();
            boolean combineCommandAliases = Settings.COMBINE_COMMAND_ALIASES.isTrue();

            if (!logUnknownCommands || combineCommandAliases) {
                Command command = plugin.getServer().getPluginCommand(commandName);
                if (command == null) {
                    try {
                        command = plugin.getServer().getCommandMap().getCommand(commandName);
                    } catch (NoSuchMethodError ignored) {
                        /* Ignored, Bukkit 1.8 has no such method */
                    }
                }
                if (command == null) {
                    if (!logUnknownCommands) {
                        return;
                    }
                } else if (combineCommandAliases) {
                    commandName = command.getName();
                }
            }
            new CommandProcessor(commandName).queue();
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }
}
