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
package com.djrapitops.plan.commands.use;

import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BukkitCommand implements CommandExecutor, TabCompleter {

    private final RunnableFactory runnableFactory;
    private final ErrorLogger errorLogger;
    private final Subcommand command;

    public BukkitCommand(
            RunnableFactory runnableFactory,
            ErrorLogger errorLogger,
            Subcommand command
    ) {
        this.runnableFactory = runnableFactory;
        this.errorLogger = errorLogger;
        this.command = command;
    }

    private CMDSender getSender(CommandSender sender) {
        if (sender instanceof Player) {
            return new BukkitPlayerCMDSender((Player) sender);
        } else {
            return new BukkitCMDSender(sender);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (command.getRequiredPermissions().stream().anyMatch(permission -> !sender.hasPermission(permission))) {
            return true;
        }
        runnableFactory.create(() -> {
            try {
                command.getExecutor().accept(getSender(sender), new Arguments(args));
            } catch (Exception e) {
                errorLogger.error(e, ErrorContext.builder()
                        .related(sender.getClass())
                        .related(label + " " + Arrays.toString(args))
                        .build());
            }
        }).runTaskAsynchronously();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (command.getRequiredPermissions().stream().anyMatch(permission -> !sender.hasPermission(permission))) {
            return Collections.emptyList();
        }
        try {
            return command.getArgumentResolver().apply(getSender(sender), new Arguments(args));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder()
                    .related(sender.getClass())
                    .related("tab completion")
                    .related(label + " " + Arrays.toString(args))
                    .build());
            return Collections.emptyList();
        }
    }
}