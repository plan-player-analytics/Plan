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
package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.db.access.transactions.events.CommandStoreTransaction;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DataGatheringSettings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import javax.inject.Inject;

/**
 * Event Listener for PlayerCommandPreprocessEvents.
 *
 * @author Rsl1122
 */
public class CommandListener implements Listener {

    private final Plan plugin;
    private final PlanConfig config;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ErrorHandler errorHandler;

    @Inject
    public CommandListener(
            Plan plugin,
            PlanConfig config,
            ServerInfo serverInfo,
            DBSystem dbSystem,
            ErrorHandler errorHandler
    ) {
        this.plugin = plugin;
        this.config = config;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.errorHandler = errorHandler;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        boolean hasIgnorePermission = event.getPlayer().hasPermission(Permissions.IGNORE_COMMAND_USE.getPermission());
        if (event.isCancelled() || hasIgnorePermission) {
            return;
        }

        try {
            actOnCommandEvent(event);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void actOnCommandEvent(PlayerCommandPreprocessEvent event) {
        String commandName = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        boolean logUnknownCommands = config.isTrue(DataGatheringSettings.LOG_UNKNOWN_COMMANDS);
        boolean combineCommandAliases = config.isTrue(DataGatheringSettings.COMBINE_COMMAND_ALIASES);

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
        dbSystem.getDatabase().executeTransaction(new CommandStoreTransaction(serverInfo.getServerUUID(), commandName));
    }

    private Command getBukkitCommand(String commandName) {
        Command command = plugin.getServer().getPluginCommand(commandName);
        if (command == null) {
            try {
                command = plugin.getServer().getCommandMap().getCommand(commandName);
            } catch (NoSuchMethodError ignored) {
                /* Ignored, Bukkit 1.8 has no such method. This method is from Paper */
            }
        }
        return command;
    }
}
