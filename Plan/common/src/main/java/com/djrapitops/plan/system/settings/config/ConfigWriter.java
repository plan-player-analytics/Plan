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
package com.djrapitops.plan.system.settings.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Writer for parsing {@link Config} into file-lines.
 * <p>
 * ConfigReader can write a single file at a time, so it is NOT thread safe.
 *
 * @author Rsl1122
 */
public class ConfigWriter {

    private final Path outputPath;
    private int indent;

    public ConfigWriter(Path outputPath) {
        this.outputPath = outputPath;
    }

    public void write(ConfigNode writing) throws IOException {
        ConfigNode storedParent = writing.parent;
        writing.updateParent(null);

        Files.write(outputPath, parseLines(writing), StandardCharsets.UTF_8);

        writing.updateParent(storedParent);
    }

    private List<String> parseLines(ConfigNode writing) {
        List<String> lines = new ArrayList<>();

        indent = writing.getNodeDepth() * 4;

        addComment(writing.comment, lines);
        addValue(writing.key, writing.value, lines);

        return lines;
    }

    private void addValue(String key, String value, Collection<String> lines) {
        if (value == null) {
            return;
        }
        if (value.contains("\n")) {
            addListValue(key, value.split("\\n"), lines);
        } else {
            addNormalValue(key, value, lines);
        }
    }

    private void addNormalValue(String key, String value, Collection<String> lines) {
        StringBuilder lineBuilder = indentedBuilder().append(key).append(": ").append(value);
        lines.add(lineBuilder.toString());
    }

    private void addListValue(String key, String[] listItems, Collection<String> lines) {
        addNormalValue(key, "", lines);
        for (String listItem : listItems) {
            listItem = listItem.trim();
            if (listItem.isEmpty()) {
                continue;
            }
            StringBuilder lineBuilder = indentedBuilder().append(listItem);
            lines.add(lineBuilder.toString());
        }
    }

    private void addComment(Iterable<String> comments, Collection<String> lines) {
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
        for (int i = 0; i < indent; i++) {
            lineBuilder.append(' ');
        }
    }
}
