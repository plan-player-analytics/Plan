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

import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.TriConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class CommandWithSubcommands extends Subcommand {

    private final List<Subcommand> subcommands;
    private BiConsumer<CMDSender, Arguments> fallback;
    private TriConsumer<RuntimeException, CMDSender, Arguments> exceptionHandler;
    private ColorScheme colors;

    private final Locale locale;

    private CommandWithSubcommands(Locale locale) {
        this.locale = locale;
        subcommands = new ArrayList<>();
    }

    public static Builder builder(Locale locale) {
        return new Builder(locale);
    }

    private List<Subcommand> getPermittedSubcommands(CMDSender sender) {
        return subcommands.stream().filter(sender::hasAllPermissionsFor).collect(Collectors.toList());
    }

    public List<Subcommand> getSubcommands() {
        return subcommands;
    }

    public Optional<Subcommand> findSubCommand(@Untrusted Arguments arguments) {
        return arguments.get(0).flatMap(alias -> {
            for (Subcommand subcommand : subcommands) {
                if (subcommand.getAliases().contains(alias)) {
                    return Optional.of(subcommand);
                }
            }
            return Optional.empty();
        });
    }

    public void onHelp(CMDSender sender, @Untrusted Arguments arguments) {
        List<Subcommand> hasPermissionFor = getPermittedSubcommands(sender);
        sender.buildMessage()
                .addPart(locale.getString(CommandLang.HEADER_HELP, getPrimaryAlias()))
                .newLine().newLine()
                .apply(new HelpFormatter(sender, colors, getPrimaryAlias(), hasPermissionFor)::addSubcommands)
                .newLine().newLine()
                .addPart(locale.getString(CommandLang.FOOTER_HELP, getPrimaryAlias()))
                .hover("")
                .send();
    }

    public void onInDepthHelp(CMDSender sender) {
        List<Subcommand> hasPermissionFor = getPermittedSubcommands(sender);
        sender.buildMessage()
                .addPart(locale.getString(CommandLang.HEADER_HELP, getPrimaryAlias()))
                .newLine().newLine()
                .apply(new HelpFormatter(sender, colors, getPrimaryAlias(), hasPermissionFor)::addInDepthSubcommands)
                .newLine().newLine()
                .addPart(locale.getString(CommandLang.FOOTER_HELP, getPrimaryAlias()))
                .hover("")
                .send();
    }

    public void onCommand(CMDSender sender, @Untrusted Arguments arguments) {
        if (sender.isMissingPermissionsFor(this)) {
            sender.send(locale.getString(CommandLang.FAIL_NO_PERMISSION) + " " + getRequiredPermissions());
            return;
        }
        try {
            executeCommand(sender, arguments);
        } catch (RuntimeException e) {
            exceptionHandler.accept(e, sender, arguments);
        }
    }

    public void executeCommand(CMDSender sender, @Untrusted Arguments arguments) {
        @Untrusted Optional<String> gotAlias = arguments.get(0);
        if (gotAlias.isPresent()) {
            @Untrusted String alias = gotAlias.get();
            if ("help".equals(alias)) {
                onHelp(sender, arguments);
                return;
            } else if ("?".equals(alias)) {
                onInDepthHelp(sender);
                return;
            } else {
                for (Subcommand subcommand : subcommands) {
                    if (subcommand.getAliases().contains(alias)) {
                        if (sender.isMissingPermissionsFor(subcommand)) {
                            sender.send(locale.getString(CommandLang.FAIL_NO_PERMISSION) + " " + subcommand.getRequiredPermissions());
                            continue;
                        }
                        subcommand.getExecutor().accept(sender, arguments.removeFirst());
                        return;
                    }
                }
            }
        }
        // Use fallback if no command is found.
        fallback.accept(sender, arguments);
    }

    public List<String> onTabComplete(CMDSender sender, @Untrusted Arguments arguments) {
        @Untrusted Optional<String> gotAlias = arguments.get(0);
        List<String> options = new ArrayList<>();
        if (gotAlias.isPresent()) {
            subcommandsLoop:
            for (Subcommand subcommand : subcommands) {
                if (sender.isMissingPermissionsFor(subcommand)) {
                    continue;
                }
                for (String alias : subcommand.getAliases()) {
                    if (alias.equals(gotAlias.get())) {
                        options.addAll(subcommand.getArgumentResolver().apply(sender, arguments.removeFirst()));
                        break subcommandsLoop;
                    } else if (alias.startsWith(gotAlias.get())) {
                        options.add(alias);
                    }
                }
            }
        } else {
            for (Subcommand subcommand : subcommands) {
                options.add(subcommand.getPrimaryAlias());
            }
        }
        Collections.sort(options);
        return options;
    }

    public static class Builder extends Subcommand.Builder<Builder> {
        private final CommandWithSubcommands command;

        private Builder(Locale locale) {
            this(new CommandWithSubcommands(locale));
        }

        private Builder(CommandWithSubcommands command) {
            super(command);
            this.command = command;
        }

        public Builder subcommand(Subcommand subcommand) {
            if (subcommand != null) {
                command.subcommands.add(subcommand);
            }
            return this;
        }

        public Builder subcommandOnCondition(boolean condition, Subcommand subcommand) {
            if (condition) command.subcommands.add(subcommand);
            return this;
        }

        public Builder fallback(BiConsumer<CMDSender, Arguments> executor) {
            command.fallback = executor;
            return this;
        }

        public Builder fallbackOrHelpFallback(
                BiPredicate<CMDSender, Arguments> canBeUsed,
                BiConsumer<CMDSender, Arguments> executor
        ) {
            return fallback((sender, arguments) -> {
                if (canBeUsed.test(sender, arguments)) {
                    executor.accept(sender, arguments);
                } else {
                    command.onHelp(sender, arguments);
                }
            });
        }

        public Builder exceptionHandler(TriConsumer<RuntimeException, CMDSender, Arguments> exceptionHandler) {
            command.exceptionHandler = exceptionHandler;
            return this;
        }

        public Builder colorScheme(ColorScheme colors) {
            command.colors = colors;
            return this;
        }

        @Override
        public CommandWithSubcommands build() {
            super.onCommand(command::onCommand);
            super.onTabComplete(command::onTabComplete);
            super.build();
            if (command.fallback == null) fallback(command::onHelp);
            if (command.exceptionHandler == null) exceptionHandler((error, sender, arguments) -> {throw error;});
            if (command.colors == null) colorScheme(new ColorScheme("§2", "§7", "§f"));
            return command;
        }
    }
}
