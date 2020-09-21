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
package com.djrapitops.plan.command.use;

import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeeCommand extends Command implements TabExecutor {

    private final RunnableFactory runnableFactory;
    private final Subcommand command;

    public BungeeCommand(
            RunnableFactory runnableFactory,
            Subcommand command,
            String name
    ) {
        super(name);
        this.runnableFactory = runnableFactory;

        this.command = command;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        runnableFactory.create("", new AbsRunnable() {
            @Override
            public void run() {
                if (sender instanceof ProxiedPlayer) {
                    command.getExecutor().accept(new BungeePlayerCMDSender((ProxiedPlayer) sender), new Arguments(args));
                } else {
                    command.getExecutor().accept(new BungeeCMDSender(sender), new Arguments(args));
                }
            }
        }).runTaskAsynchronously();
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return command.getArgumentResolver().apply(new BungeePlayerCMDSender((ProxiedPlayer) sender), new Arguments(args));
        } else {
            return command.getArgumentResolver().apply(new BungeeCMDSender(sender), new Arguments(args));
        }
    }
}
