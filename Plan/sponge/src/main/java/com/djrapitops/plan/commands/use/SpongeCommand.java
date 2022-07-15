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

import com.djrapitops.plan.PlanSpongeComponent;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.network.RconConnection;
import org.spongepowered.api.service.permission.Subject;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpongeCommand implements Command.Raw {

    private final RunnableFactory runnableFactory;
    private final Supplier<PlanSpongeComponent> componentSupplier;
    private PlanSpongeComponent commandComponent;
    private ErrorLogger errorLogger;
    private Subcommand subcommand;

    public SpongeCommand(
            RunnableFactory runnableFactory,
            Supplier<PlanSpongeComponent> componentSupplier,
            Subcommand initialCommand
    ) {
        this.runnableFactory = runnableFactory;
        this.componentSupplier = componentSupplier;
        this.commandComponent = componentSupplier.get();
        this.errorLogger = commandComponent.errorLogger();
        this.subcommand = initialCommand;
    }

    private synchronized Subcommand getCommand() {
        PlanSpongeComponent component = componentSupplier.get();
        // Check if the component has changed, if it has, update the command and error logger.
        if (commandComponent != component) {
            errorLogger = component.errorLogger();
            subcommand = component.planCommand().build();
            commandComponent = component;
        }

        return subcommand;
    }

    private CMDSender getSender(CommandCause cause) {
        return getSender(cause.subject(), cause.audience());
    }

    private CMDSender getSender(Subject subject, Audience audience) {
        if (subject instanceof ServerPlayer || subject instanceof RconConnection) {
            return new SpongePlayerCMDSender(subject, audience);
        } else {
            return new SpongeCMDSender(subject, audience);
        }
    }

    private Component convertLegacy(String legacy) {
        return LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Override
    public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) {
        runnableFactory.create(() -> {
            try {
                getCommand().getExecutor().accept(getSender(cause), new Arguments(arguments.input()));
            } catch (Exception e) {
                errorLogger.error(e, ErrorContext.builder()
                        .related(cause.subject().getClass())
                        .related(arguments)
                        .build());
            }
        }).runTaskAsynchronously();
        return CommandResult.success();
    }

    @Override
    public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) {
        try {
            return getCommand().getArgumentResolver()
                    .apply(getSender(cause), new Arguments(arguments.input()))
                    .stream()
                    .map(CommandCompletion::of)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder()
                    .related(cause.subject().getClass())
                    .related("tab completion")
                    .related(arguments)
                    .build());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean canExecute(CommandCause cause) {
        for (String requiredPermission : getCommand().getRequiredPermissions()) {
            if (!cause.subject().hasPermission(requiredPermission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<Component> shortDescription(CommandCause cause) {
        return Optional.ofNullable(getCommand().getDescription()).map(this::convertLegacy);
    }

    @Override
    public Optional<Component> extendedDescription(CommandCause cause) {
        return Optional.ofNullable(getCommand().getDescription()).map(this::convertLegacy);
    }

    @Override
    public Component usage(CommandCause cause) {
        TextComponent.Builder builder = Component.text();
        List<String> lines = getCommand().getInDepthDescription();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            builder.append(convertLegacy(line));
            if (i < lines.size() - 1) {
                builder.append(Component.newline());
            }
        }
        return builder.build();
    }
}
