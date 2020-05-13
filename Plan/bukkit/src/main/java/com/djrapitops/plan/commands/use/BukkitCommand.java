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

import com.djrapitops.plan.commands.Arguments;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitCommand implements CommandExecutor {

    private final RunnableFactory runnableFactory;
    private final Subcommand command;

    public BukkitCommand(RunnableFactory runnableFactory, Subcommand command) {
        this.runnableFactory = runnableFactory;
        this.command = command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        runnableFactory.create("", new AbsRunnable() {
            @Override
            public void run() {
                if (sender instanceof Player) {
                    command.getExecutor().accept(new BukkitPlayerCMDSender((Player) sender), new Arguments(args));
                } else {
                    command.getExecutor().accept(new BukkitCMDSender(sender), new Arguments(args));
                }
            }
        }).runTaskAsynchronously();
        return true;
    }
}