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
import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class BungeeCommand extends Command implements TabExecutor {

    private final RunnableFactory runnableFactory;
    private final ErrorLogger errorLogger;
    private final Subcommand command;

    public BungeeCommand(
            RunnableFactory runnableFactory,
            ErrorLogger errorLogger,
            Subcommand command, String name
    ) {
        super(name, Permissions.USE_COMMAND.getPermission());
        this.runnableFactory = runnableFactory;
        this.errorLogger = errorLogger;

        this.command = command;
    }

    private CMDSender getSender(CommandSender sender) {
        if (sender instanceof ProxiedPlayer) {
            return new BungeePlayerCMDSender((ProxiedPlayer) sender);
        } else {
            return new BungeeCMDSender(sender);
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        runnableFactory.create(() -> {
            try {
                command.getExecutor().accept(getSender(sender), new Arguments(args));
            } catch (Exception e) {
                errorLogger.error(e, ErrorContext.builder()
                        .related(sender.getClass())
                        .related(Arrays.toString(args))
                        .build());
            }
        }).runTaskAsynchronously();
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        try {
            return command.getArgumentResolver().apply(getSender(sender), new Arguments(args));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder()
                    .related(sender.getClass())
                    .related("tab completion")
                    .related(Arrays.toString(args))
                    .build());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BungeeCommand that = (BungeeCommand) o;
        return Objects.equals(runnableFactory, that.runnableFactory) &&
                Objects.equals(errorLogger, that.errorLogger) &&
                Objects.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), runnableFactory, errorLogger, command);
    }
}
