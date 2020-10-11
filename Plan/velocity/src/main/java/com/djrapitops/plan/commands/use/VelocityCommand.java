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
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VelocityCommand implements Command {

    private final RunnableFactory runnableFactory;
    private final ErrorLogger errorLogger;
    private final Subcommand command;

    public VelocityCommand(RunnableFactory runnableFactory, ErrorLogger errorLogger, Subcommand command) {
        this.runnableFactory = runnableFactory;
        this.errorLogger = errorLogger;
        this.command = command;
    }

    @Override
    public void execute(CommandSource source, String[] args) {
        runnableFactory.create("", new AbsRunnable() {
            @Override
            public void run() {
                try {
                    command.getExecutor().accept(getSender(source), new Arguments(args));
                } catch (Exception e) {
                    errorLogger.log(L.ERROR, e, ErrorContext.builder()
                            .related(source.getClass())
                            .related(Arrays.toString(args))
                            .build());
                }
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
    public List<String> suggest(CommandSource source, String[] currentArgs) {
        try {
            return command.getArgumentResolver().apply(getSender(source), new Arguments(currentArgs));
        } catch (Exception e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder()
                    .related(source.getClass())
                    .related("tab completion")
                    .related(Arrays.toString(currentArgs))
                    .build());
            return Collections.emptyList();
        }
    }
}