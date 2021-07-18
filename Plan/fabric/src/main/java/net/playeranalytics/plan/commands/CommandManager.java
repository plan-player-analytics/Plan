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

import java.security.Permission;
import java.util.concurrent.CompletableFuture;

public class CommandManager {

    private final CommandDispatcher<ServerCommandSource> dispatcher;
    private LiteralArgumentBuilder<ServerCommandSource> root;

    public CommandManager(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static CompletableFuture<Suggestions> arguments(final Subcommand subcommand, final CommandContext<ServerCommandSource> ctx, final SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(subcommand.getArgumentResolver().apply((CMDSender) ctx.getSource(), new Arguments(new String[0])), builder);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx, Subcommand subcommand) {
        String arguments;
        try {
            arguments = StringArgumentType.getString(ctx, "arguments");
        } catch (IllegalArgumentException e) {
            arguments = "";
        }
        try {
            subcommand.getExecutor().accept((CMDSender) ctx.getSource(), new Arguments(arguments));
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendError(new LiteralText(e.getMessage()));
            return -1;
        }

    }

    private void build() {
        dispatcher.register(root);
    }

    public void registerRoot(Subcommand subcommand) {
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
                if (!Permissions.check(src, permission, 2)) return false;
            }
            return true;
        });

        literal.then(arguments);
        return literal;
    }

}
