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
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VelocityCommand implements SimpleCommand {

    private final RunnableFactory runnableFactory;
    private final ErrorLogger errorLogger;
    private final Subcommand command;

    public VelocityCommand(RunnableFactory runnableFactory, ErrorLogger errorLogger, Subcommand command) {
        this.runnableFactory = runnableFactory;
        this.errorLogger = errorLogger;
        this.command = command;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (command.getRequiredPermissions().stream().anyMatch(permission -> !source.hasPermission(permission))) {
            return;
        }
        runnableFactory.create(() -> {
            try {
                command.getExecutor().accept(getSender(source), new Arguments(args));
            } catch (Exception e) {
                errorLogger.error(e, ErrorContext.builder()
                        .related(source.getClass())
                        .related(Arrays.toString(args))
                        .build());
            }
        }).runTaskAsynchronously();
    }

    private CMDSender getSender(CommandSource source) {
        if (source instanceof Player) {
            return new VelocityPlayerCMDSender((Player) source);
        } else {
            return new VelocityCMDSender(source);
        }
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] currentArgs = invocation.arguments();
        if (command.getRequiredPermissions().stream().anyMatch(permission -> !source.hasPermission(permission))) {
            return Collections.emptyList();
        }
        try {
            return command.getArgumentResolver().apply(getSender(source), new Arguments(currentArgs));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder()
                    .related(source.getClass())
                    .related("tab completion")
                    .related(Arrays.toString(currentArgs))
                    .build());
            return Collections.emptyList();
        }
    }
}