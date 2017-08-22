package main.java.com.djrapitops.plan.data.listeners;

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
    private final DataCache dataCache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanCommandPreprocessListener(Plan plugin) {
        this.plugin = plugin;
        dataCache = plugin.getDataCache();
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
        if (player.hasPermission(Permissions.IGNORE_COMMANDUSE.getPermission())) {
            return;
        }

        String commandName = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        boolean doNotLogUnknownCommands = Settings.LOG_UNKNOWN_COMMANDS.isTrue();
        boolean combineCommandAliasesToMainCommand = Settings.COMBINE_COMMAND_ALIASES.isTrue();

        if (doNotLogUnknownCommands || combineCommandAliasesToMainCommand) {
            Command command = plugin.getServer().getPluginCommand(commandName);
            if (command == null) {
                if (doNotLogUnknownCommands) {
                    return;
                }
            } else if (combineCommandAliasesToMainCommand) {
                commandName = command.getName();
            }
        }
        // TODO Command Usage -> DB Save Processor
        dataCache.handleCommand(commandName);
    }
}
