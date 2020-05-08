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

import com.djrapitops.plugin.command.ColorScheme;
import org.apache.commons.text.TextStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HelpFormatter {

    private final ColorScheme colors;
    private final String mainCommand;
    private final List<Subcommand> subcommands;

    public HelpFormatter(
            ColorScheme colors, String mainCommand, List<Subcommand> subcommands
    ) {
        this.colors = colors;
        this.mainCommand = mainCommand;
        this.subcommands = subcommands;
    }

    public MessageBuilder addSubcommands(MessageBuilder message) {
        for (Subcommand subcommand : subcommands) {
            String alias = subcommand.getPrimaryAlias();
            message.addPart(colors.getMainColor() + mainCommand + " " + alias + "---")
                    .hover(argumentsAndAliases(subcommand.getArguments(), subcommand.getAliases()));
            message.addPart(colors.getSecondaryColor() + subcommand.getDescription())
                    .hover(subcommand.getInDepthDescription());
            message.tabular("---");
        }
        return message;
    }

    private List<String> argumentsAndAliases(List<Subcommand.ArgumentDescriptor> descriptors, Set<String> aliases) {
        List<String> lines = new ArrayList<>();
        lines.add(colors.getTertiaryColor() + "Arguments:");
        for (Subcommand.ArgumentDescriptor descriptor : descriptors) {
            if (descriptor.isRequired()) {
                lines.add("  " + colors.getMainColor() + "§l<" + descriptor.getName() + ">§r " + colors.getSecondaryColor() + descriptor.getDescription());
            } else {
                lines.add("  " + colors.getMainColor() + "[" + descriptor.getName() + "] " + colors.getSecondaryColor() + descriptor.getDescription());
            }
        }
        lines.add(colors.getTertiaryColor() + "Aliases:" + new TextStringBuilder().appendWithSeparators(aliases, " | ").toString());
        return lines;
    }

}
