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
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.RconSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SpongeCommand implements CommandCallable {

    private final RunnableFactory runnableFactory;
    private final ErrorLogger errorLogger;
    private final Subcommand command;

    public SpongeCommand(
            RunnableFactory runnableFactory,
            ErrorLogger errorLogger, Subcommand command
    ) {
        this.runnableFactory = runnableFactory;
        this.errorLogger = errorLogger;
        this.command = command;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) {
        runnableFactory.create(() -> {
            try {
                command.getExecutor().accept(getSender(source), new Arguments(arguments));
            } catch (Exception e) {
                errorLogger.error(e, ErrorContext.builder()
                        .related(source.getClass())
                        .related(arguments)
                        .build());
            }
        }).runTaskAsynchronously();
        return CommandResult.success();
    }

    private CMDSender getSender(CommandSource source) {
        if (source instanceof Player || source instanceof RconSource) {
            return new SpongePlayerCMDSender(source);
        } else {
            return new SpongeCMDSender(source);
        }
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) {
        try {
            return command.getArgumentResolver()
                    .apply(getSender(source), new Arguments(arguments));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder()
                    .related(source.getClass())
                    .related("tab completion")
                    .related(arguments)
                    .build());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean testPermission(CommandSource source) {
        for (String requiredPermission : command.getRequiredPermissions()) {
            if (!source.hasPermission(requiredPermission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.ofNullable(command.getDescription()).map(Text::of);
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.ofNullable(command.getDescription()).map(Text::of);
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of(command.getInDepthDescription());
    }
}
