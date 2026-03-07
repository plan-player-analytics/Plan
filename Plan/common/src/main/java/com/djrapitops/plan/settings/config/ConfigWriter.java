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
package com.djrapitops.plan.settings.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Writer for parsing {@link Config} into file-lines.
 * <p>
 * ConfigReader can write a single file at a time, so it is NOT thread safe.
 *
 * @author AuroraLS3
 */
public class ConfigWriter {

    private Path outputPath;
    private int indent;

    /**
     * Create a new ConfigWriter that doesn't write anywhere.
     */
    public ConfigWriter() {
    }

    /**
     * Create a new ConfigWriter that writes to a Path.
     *
     * @param outputPath Path to write to.
     */
    public ConfigWriter(Path outputPath) {
        this.outputPath = outputPath;
    }

    /**
     * Write a {@link ConfigNode} into the given resource.
     *
     * @param writing ConfigNode to write.
     * @throws IOException           If the Path given to constructor can not be written to.
     * @throws IllegalStateException If the Path is null
     */
    public void write(ConfigNode writing) throws IOException {
        if (outputPath == null) throw new IllegalStateException("Output path was null.");

        ConfigNode storedParent = writing.parent;
        writing.updateParent(null); // Ensure that the node is written as the root node (Paths match)

        Path dir = outputPath.getParent();
        if (!Files.isSymbolicLink(dir)) Files.createDirectories(dir);
        Files.write(outputPath, createLines(writing), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        writing.updateParent(storedParent); // Restore state of the config node
    }

    /**
     * Parse the lines of a {@link ConfigNode}.
     * <p>
     * "Write" the lines into a List.
     *
     * @param writing ConfigNode to "write"
     * @return List of lines that would be written.
     */
    public List<String> createLines(ConfigNode writing) {
        List<String> lines = new ArrayList<>();

        dfsTreeTraverseLineResolve(writing, lines);

        return lines;
    }

    private void dfsTreeTraverseLineResolve(ConfigNode writing, Collection<String> lines) {
        Map<String, ConfigNode> children = writing.childNodes;
        for (String key : writing.getNodeOrder()) {
            ConfigNode node = children.get(key);
            // node is null:       Inconsistent config node state
            // value is null:      Has no value (empty)
            // nodeOrder is empty: Has no children
            if (node == null || node.value == null && node.nodeOrder.isEmpty()) {
                continue;
            }

            indent = node.getNodeDepth() * 4;
            addComment(node.comment, lines);
            addValue(node, lines);

            dfsTreeTraverseLineResolve(node, lines);
        }
    }

    private void addValue(ConfigNode node, Collection<String> lines) {
        String key = node.key;
        String value = node.value;

        if (value == null || value.isEmpty()) {
            addKey(key, lines);
        } else if (Strings.CI.contains(value, "\n")) {
            // List values include newline characters,
            // see ConfigValueParser.StringListParser
            addListValue(key, StringUtils.split(value, "\n"), lines);
        } else {
            addNormalValue(key, value, lines);
        }
    }

    private void addKey(String key, Collection<String> lines) {
        // Key:
        lines.add(indentedBuilder().append(key).append(":").toString());
    }

    private void addNormalValue(String key, String value, Collection<String> lines) {
        // Key: value
        StringBuilder lineBuilder = indentedBuilder().append(key).append(": ").append(value);
        lines.add(lineBuilder.toString());
    }

    private void addListValue(String key, String[] listItems, Collection<String> lines) {
        // Key:
        //   - List item
        addKey(key, lines);
        for (String listItem : listItems) {
            listItem = listItem.trim();
            if (listItem.isEmpty()) {
                continue;
            }
            addListItem(listItem, lines);
        }
    }

    private void addListItem(String listItem, Collection<String> lines) {
        StringBuilder lineBuilder = indentedBuilder()
                .append("  ") // Append 2 spaces to adhere to yml format for lists
                .append(listItem);
        lines.add(lineBuilder.toString());
    }

    private void addComment(Iterable<String> comments, Collection<String> lines) {
        // # Comment line
        for (String comment : comments) {
            StringBuilder lineBuilder = indentedBuilder().append("# ").append(comment);
            lines.add(lineBuilder.toString());
        }
    }

    private StringBuilder indentedBuilder() {
        StringBuilder lineBuilder = new StringBuilder();
        indent(indent, lineBuilder);
        return lineBuilder;
    }

    private void indent(int indent, StringBuilder lineBuilder) {
        lineBuilder.append(" ".repeat(Math.max(0, indent)));
    }
}
