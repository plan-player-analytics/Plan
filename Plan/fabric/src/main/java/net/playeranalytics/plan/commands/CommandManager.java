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
package net.playeranalytics.plan.commands;

import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.commands.use.CommandWithSubcommands;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.playeranalytics.plan.PlanFabric;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import java.util.concurrent.CompletableFuture;

public class CommandManager {

    private final CommandDispatcher<ServerCommandSource> dispatcher;
    private RunnableFactory runnableFactory;
    private LiteralArgumentBuilder<ServerCommandSource> root;
    private final PlanFabric plugin;

    public CommandManager(CommandDispatcher<ServerCommandSource> dispatcher, PlanFabric plugin) {
        this.dispatcher = dispatcher;
        this.plugin = plugin;
    }

    public static boolean checkPermission(ServerCommandSource src, String permission) {
        if (isPermissionsApiAvailable()) {
            return Permissions.check(src, permission, 2);
        } else if (src.hasPermissionLevel(2)) {
            return true;
        } else {
            return switch (permission) {
                case "plan.player.self", "plan.ingame.self", "plan.register.self", "plan.unregister.self", "plan.json.self" -> true;
                default -> false;
            };
        }
    }

    public static boolean isPermissionsApiAvailable() {
        try {
            Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");
            return true;
        } catch (ClassNotFoundException e) {
            return false; // not available
        }
    }

    public CompletableFuture<Suggestions> arguments(final Subcommand subcommand, final CommandContext<ServerCommandSource> ctx, final SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(subcommand.getArgumentResolver().apply((CMDSender) ctx.getSource(), new Arguments(new String[0])), builder);
    }

    private int execute(CommandContext<ServerCommandSource> ctx, Subcommand subcommand) {
        runnableFactory.create(() -> {
            try {
                subcommand.getExecutor().accept((CMDSender) ctx.getSource(), new Arguments(getCommandArguments(ctx)));
            } catch (Exception e) {
                ctx.getSource().sendError(new LiteralText("An internal error occurred, see the console for details."));
                plugin.getSystem().getErrorLogger().error(e, ErrorContext.builder()
                        .related(ctx.getSource().getClass())
                        .related(subcommand.getPrimaryAlias() + " " + getCommandArguments(ctx))
                        .build());
            }
        }).runTaskAsynchronously();
        return 1;
    }

    private String getCommandArguments(CommandContext<ServerCommandSource> ctx) {
        String arguments;
        try {
            arguments = StringArgumentType.getString(ctx, "arguments");
        } catch (IllegalArgumentException e) {
            arguments = "";
        }
        return arguments;
    }

    private void build() {
        dispatcher.register(root);
    }

    public void registerRoot(Subcommand subcommand, RunnableFactory runnableFactory) {
        this.runnableFactory = runnableFactory;
        root = buildCommand(subcommand, subcommand.getPrimaryAlias());
        if (subcommand instanceof CommandWithSubcommands withSubcommands) {
            for (Subcommand cmd : withSubcommands.getSubcommands()) {
                registerChild(cmd, root);
            }
        }
        build();
    }

    public void registerChild(Subcommand subcommand, ArgumentBuilder<ServerCommandSource, ?> parent) {
        for (String alias : subcommand.getAliases()) {
            LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = buildCommand(subcommand, alias);
            if (subcommand instanceof CommandWithSubcommands withSubcommands) {
                for (Subcommand cmd : withSubcommands.getSubcommands()) {
                    registerChild(cmd, argumentBuilder);
                }
            }
            parent.then(argumentBuilder);
        }
    }

    private LiteralArgumentBuilder<ServerCommandSource> buildCommand(Subcommand subcommand, String alias) {
        RequiredArgumentBuilder<ServerCommandSource, String> arguments = RequiredArgumentBuilder.argument("arguments", StringArgumentType.greedyString());
        arguments.suggests((context, builder) -> arguments(subcommand, context, builder));
        arguments.executes(ctx -> execute(ctx, subcommand));
        LiteralArgumentBuilder<ServerCommandSource> literal = LiteralArgumentBuilder.literal(alias);
        literal.executes(ctx -> execute(ctx, subcommand));
        literal.requires(src -> {
            for (String permission : subcommand.getRequiredPermissions()) {
                if (!checkPermission(src, permission)) return false;
            }
            return true;
        });

        literal.then(arguments);
        return literal;
    }

}
