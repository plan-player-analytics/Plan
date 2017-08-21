package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.cache.DataCache;
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
public class PlanCommandPreprocessListener implements Listener {

    private final Plan plugin;
    private final DataCache handler;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanCommandPreprocessListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
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

        String commandName = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        boolean doNotLogUnknownCommands = Settings.LOG_UNKNOWN_COMMANDS.isTrue();
        boolean combineCommandAliasesToMainCommand = Settings.COMBINE_COMMAND_ALIASES.isTrue();

        if (doNotLogUnknownCommands || combineCommandAliasesToMainCommand) {
            Command command = plugin.getServer().getPluginCommand(commandName);
            if (command == null) {
                if (doNotLogUnknownCommands) {
                    Log.debug("Ignored command, command is unknown");
                    return;
                }
            } else if (combineCommandAliasesToMainCommand) {
                commandName = command.getName();
            }
        }

        Player player = event.getPlayer();

        if (player.hasPermission(Permissions.IGNORE_COMMANDUSE.getPermission())) {
            Log.debug("Ignored command, player had ignore permission.");
            return;
        }

        handler.handleCommand(commandName);
    }
}
