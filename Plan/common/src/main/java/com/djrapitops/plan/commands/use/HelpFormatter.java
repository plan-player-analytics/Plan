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

    private final CMDSender sender;
    private final ColorScheme colors;
    private final String mainCommand;
    private final List<Subcommand> subcommands;

    public HelpFormatter(
            CMDSender sender, ColorScheme colors, String mainCommand, List<Subcommand> subcommands
    ) {
        this.sender = sender;
        this.colors = colors;
        this.mainCommand = mainCommand;
        this.subcommands = subcommands;
    }

    public MessageBuilder addSubcommands(MessageBuilder message) {
        MessageBuilder toReturn = message;
        String m = colors.getMainColor();
        String s = colors.getSecondaryColor();
        String asString = subcommands.stream()
                .map(cmd ->
                        m + mainCommand + " " + cmd.getPrimaryAlias() +
                                (sender.supportsChatEvents() ? " " : " " + cmd.getArgumentsAsString()) + "***" +
                                s + cmd.getDescription() + "\n"
                ).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        List<String[]> table = sender.getFormatter().tableAsParts(asString, "***");

        for (int i = 0; i < table.size(); i++) {
            Subcommand cmd = subcommands.get(i);
            if (cmd.getDescription() == null) continue;

            String[] row = table.get(i);

            if (sender.isPlayer()) {
                toReturn = toReturn.addPart(m + "/");
            }

            if (sender.supportsChatEvents()) {
                toReturn = toReturn.addPart(row[0])
                        .hover(argumentsAndAliases(cmd, cmd.getArguments(), cmd.getAliases()))
                        .addPart(row[1])
                        .hover(cmd.getInDepthDescription())
                        .newLine();
            } else {
                toReturn = toReturn.addPart(row[0])
                        .addPart(row[1])
                        .newLine();
            }
        }

        return toReturn;
    }

    public MessageBuilder addInDepthSubcommands(MessageBuilder message) {
        MessageBuilder toReturn = message;
        String m = colors.getMainColor();
        String s = colors.getSecondaryColor();
        String asString = subcommands.stream()
                .filter(cmd -> cmd.getDescription() != null)
                .map(cmd -> {
                            TextStringBuilder builder = new TextStringBuilder(
                                    m + mainCommand + " " + cmd.getPrimaryAlias()
                            );
                            for (String description : cmd.getInDepthDescription()) {
                                builder.append("***").append(s).append(description).append('\n');
                            }

                            for (Subcommand.ArgumentDescriptor argument : cmd.getArguments()) {
                                builder.append(" ***").append(m).append(argument.isRequired() ? '<' + argument.getName() + '>' : '[' + argument.getName() + ']')
                                        .append(s).append(" ").append(argument.getDescription()).append('\n');
                            }
                            return builder.toString();
                        }
                ).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        List<String[]> table = sender.getFormatter().tableAsParts(asString, "***");

        for (String[] row : table) {
            if (sender.isPlayer()) {
                toReturn = toReturn.addPart(m + "/");
            }

            toReturn = toReturn.addPart(row[0]);
            if (row.length > 1) toReturn = toReturn.addPart(row[1]);
            toReturn = toReturn.newLine();
        }

        return toReturn;
    }

    private List<String> argumentsAndAliases(Subcommand subcommand, List<Subcommand.ArgumentDescriptor> descriptors, Set<String> aliases) {
        List<String> lines = new ArrayList<>();
        lines.add(colors.getMainColor() + subcommand.getPrimaryAlias() + colors.getTertiaryColor() + " Arguments:" + (descriptors.isEmpty() ? " none" : ""));
        for (Subcommand.ArgumentDescriptor descriptor : descriptors) {
            if (descriptor.isRequired()) {
                lines.add(colors.getMainColor() + "§l<" + descriptor.getName() + ">§r " + colors.getSecondaryColor() + descriptor.getDescription());
            } else {
                lines.add(colors.getMainColor() + "[" + descriptor.getName() + "] " + colors.getSecondaryColor() + descriptor.getDescription());
            }
        }
        lines.add("");
        lines.add(colors.getTertiaryColor() + "Aliases: " + new TextStringBuilder().appendWithSeparators(aliases, ", ").toString());
        return lines;
    }
}
