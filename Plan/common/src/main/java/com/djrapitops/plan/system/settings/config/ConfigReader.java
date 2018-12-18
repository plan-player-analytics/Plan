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

import com.djrapitops.plugin.utilities.Verify;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Reader for parsing {@link Config} out of file-lines.
 * <p>
 * ConfigReader can read a single file at a time, so it is NOT thread safe.
 *
 * @author Rsl1122
 */
public class ConfigReader implements Closeable {

    private static final ConfigValueParser.StringParser STRING_PARSER = new ConfigValueParser.StringParser();
    private final Scanner scanner;
    private ConfigNode previousNode;
    private ConfigNode parent;

    // Indent mode assumes the number of spaces used to indent the file.
    private int indentMode = 4;
    private List<String> unboundComment = new ArrayList<>();

    public ConfigReader(Path filePath) throws IOException {
        this(Files.newInputStream(filePath));
    }

    public ConfigReader(InputStream in) {
        this(new Scanner(new InputStreamReader(in, StandardCharsets.UTF_8)));
    }

    public ConfigReader(BufferedReader bufferedReader) {
        this(new Scanner(bufferedReader));
    }

    public ConfigReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public Config read() {
        Config config = new Config();

        previousNode = config;
        parent = config;

        while (scanner.hasNextLine()) {
            String line = readNewLine();
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                // Add an empty row comment in case user wants spacers
                handleCommentLine(" ");
            } else if (trimmed.startsWith("#")) {
                handleCommentLine(trimmed);
            } else {
                // Determine where the node belongs
                parent = findParent(previousNode.getNodeDepth(), findCurrentDepth(line));
                Verify.nullCheck(parent, () -> new IllegalStateException("Parent became null on line: \"" + line + "\""));
                previousNode = parseNode(trimmed);
                Verify.nullCheck(previousNode, () -> new IllegalStateException("Parsed node became null on line: \"" + line + "\""));
                handleUnboundComments();
            }
        }

        return config;
    }

    private String readNewLine() {
        String line = scanner.nextLine();

        // Removing any dangling comments
        int danglingComment = line.trim().indexOf(" #");
        if (danglingComment != -1) {
            line = line.substring(0, danglingComment);
        }
        return line;
    }

    private ConfigNode parseNode(String line) {
        String[] keyAndValue = line.split(":", 2);
        if (keyAndValue.length <= 1) {
            return handleMultiline(line);
        }
        String key = keyAndValue[0].trim();
        String value = keyAndValue[1].trim();
        return handleNewNode(key, value);
    }

    private ConfigNode handleMultiline(String line) {
        if (line.startsWith("- ")) {
            return handleListCase(line);
        } else {
            return handleMultilineString(line);
        }
    }

    private void handleCommentLine(String line) {
        unboundComment.add(line.substring(1).trim());
    }

    private void handleUnboundComments() {
        if (!unboundComment.isEmpty()) {
            previousNode.setComment(new ArrayList<>(unboundComment));
            unboundComment.clear();
        }
    }

    private ConfigNode handleMultilineString(String line) {
        if (previousNode.value == null) {
            previousNode.value = "";
        }
        // Append the new line to the end of the value.
        previousNode.value += line.trim();
        return previousNode;
    }

    private ConfigNode handleNewNode(String key, String value) {
        ConfigNode newNode = new ConfigNode(key, parent, value);
        return parent.addChild(newNode);
    }

    private ConfigNode handleListCase(String line) {
        if (previousNode.value == null) {
            previousNode.value = "";
        }
        // Append list item to the value.
        previousNode.value += "\n- " + line.substring(2).trim();
        return previousNode;
    }

    private ConfigNode findParent(int previousDepth, int currentDepth) {
        if (previousDepth < currentDepth) {
            return previousNode;
        } else if (previousDepth > currentDepth) {
            // Prevents incorrect indent in the case:
            // 1:
            //   2:
            //     3:
            // 1:
            int helperDepth = previousDepth;
            ConfigNode foundParent = previousNode;
            while (helperDepth > currentDepth) {
                helperDepth = foundParent.getNodeDepth();
                foundParent = foundParent.parent; // Moves one level up the tree
            }
            return foundParent;
        } else {
            return parent;
        }
    }

    private int findCurrentDepth(String line) {
        int indent = readIndent(line);

        // Re-define indent mode if indent is 2 for configs indented with 2 spaces.
        if (indent == 2 && indentMode == 4) {
            indentMode = indent;
        }

        int depth;
        if (indent % indentMode == 0) {
            depth = indent / indentMode;
        } else {
            depth = ((indent - (indent % indentMode)) / indentMode) + 1; // Round up
        }
        return depth;
    }

    private int readIndent(String line) {
        int indentation = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                indentation++;
            } else {
                break;
            }
        }
        return indentation;
    }

    @Override
    public void close() throws IOException {
        scanner.close();
    }
}
