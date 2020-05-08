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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Subcommand {

    private final Set<String> aliases;
    private final Set<String> requiredPermissions;
    private final List<String> inDepthDescription;
    private final List<ArgumentDescriptor> arguments;
    private String primaryAlias;
    private String description;
    private BiConsumer<CMDSender, Arguments> executor;
    private BiFunction<CMDSender, Arguments, List<String>> argumentResolver;

    Subcommand() {
        aliases = new HashSet<>();
        requiredPermissions = new HashSet<>();
        inDepthDescription = new ArrayList<>();
        arguments = new ArrayList<>();
    }

    public static SubcommandBuilder builder() {
        return new Builder();
    }

    public String getPrimaryAlias() {
        return primaryAlias;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public Set<String> getRequiredPermissions() {
        return requiredPermissions;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getInDepthDescription() {
        return inDepthDescription;
    }

    public List<ArgumentDescriptor> getArguments() {
        return arguments;
    }

    public BiConsumer<CMDSender, Arguments> getExecutor() {
        return executor;
    }

    public BiFunction<CMDSender, Arguments, List<String>> getArgumentResolver() {
        return argumentResolver;
    }

    public static class Builder implements SubcommandBuilder {
        private final Subcommand subcommand;

        private Builder() {
            this.subcommand = new Subcommand();
        }

        Builder(Subcommand subcommand) {
            this.subcommand = subcommand;
        }

        @Override
        public SubcommandBuilder alias(String alias) {
            subcommand.aliases.add(alias);
            if (subcommand.primaryAlias == null) subcommand.primaryAlias = alias;
            return this;
        }

        @Override
        public SubcommandBuilder aliases(String... aliases) {
            for (String alias : aliases) {
                alias(alias);
            }
            return this;
        }

        @Override
        public SubcommandBuilder requirePermission(String permission) {
            subcommand.requiredPermissions.add(permission);
            return this;
        }

        @Override
        public SubcommandBuilder description(String description) {
            subcommand.description = description;
            return this;
        }

        @Override
        public SubcommandBuilder inDepthDescription(String... lines) {
            subcommand.inDepthDescription.addAll(Arrays.asList(lines));
            return this;
        }

        @Override
        public SubcommandBuilder requiredArgument(String name, String description) {
            subcommand.arguments.add(new ArgumentDescriptor(name, description, true));
            return this;
        }

        @Override
        public SubcommandBuilder optionalArgument(String name, String description) {
            subcommand.arguments.add(new ArgumentDescriptor(name, description, false));
            return this;
        }

        @Override
        public SubcommandBuilder onCommand(BiConsumer<CMDSender, Arguments> executor) {
            subcommand.executor = executor;
            return this;
        }

        @Override
        public SubcommandBuilder onTabComplete(BiFunction<CMDSender, Arguments, List<String>> resolver) {
            subcommand.argumentResolver = resolver;
            return this;
        }

        @Override
        public Subcommand build() {
            if (subcommand.executor == null) throw new IllegalStateException("Command executor not defined.");
            if (subcommand.primaryAlias == null || subcommand.aliases.isEmpty()) {
                throw new IllegalStateException("Command aliases not defined.");
            }
            return subcommand;
        }
    }

    public static class ArgumentDescriptor {
        private final String name;
        private final String description;
        private final boolean required;

        public ArgumentDescriptor(String name, String description, boolean required) {
            this.name = name;
            this.description = description;
            this.required = required;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public boolean isRequired() {
            return required;
        }
    }
}
