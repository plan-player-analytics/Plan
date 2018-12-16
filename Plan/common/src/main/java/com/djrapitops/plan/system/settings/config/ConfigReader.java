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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
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
    private final InputStream in;
    private final Scanner scanner;
    private ConfigNode previousNode;
    private ConfigNode parent;

    public ConfigReader(InputStream in) {
        this.in = in;
        this.scanner = new Scanner(in);
    }

    public Config read() {
        Config config = new Config();

        previousNode = config;
        parent = config;

        while (scanner.hasNextLine()) {
            String line = readNewLine();
            // Determine where the node belongs
            parent = findParent(previousNode.getNodeDepth(), findCurrentDepth(line));
            previousNode = parseNode(line.trim());
        }

        return config;
    }

    private String readNewLine() {
        String line = scanner.nextLine();

        // Removing any dangling comments
        int danglingComment = line.indexOf(" #");
        if (danglingComment != -1) {
            line = line.substring(0, danglingComment);
        }
        return line;
    }

    private ConfigNode parseNode(String line) {
        if (line.startsWith("#")) {
            return handleCommentLine(line);
        }

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

    private ConfigNode handleCommentLine(String line) {
        previousNode.comment.add(line.substring(1).trim());
        return previousNode;
    }

    private ConfigNode handleMultilineString(String line) {
        if (previousNode.value == null) {
            previousNode.value = "";
        }
        // Append the new line to the end of the value.
        previousNode.value += STRING_PARSER.compose(line.substring(2).trim());
        return previousNode;
    }

    private ConfigNode handleNewNode(String key, String value) {
        ConfigNode newNode = new ConfigNode(key, parent, STRING_PARSER.compose(value));
        parent.addChild(newNode);
        return newNode;
    }

    private ConfigNode handleListCase(String line) {
        if (previousNode.value == null) {
            previousNode.value = "";
        }
        // Append list item to the value.
        previousNode.value += "\n- " + STRING_PARSER.compose(line.substring(2).trim());
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
            ConfigNode foundParent = parent;
            while (helperDepth > currentDepth) {
                helperDepth = parent.getNodeDepth();
                foundParent = foundParent.parent; // Moves one level up the tree
            }
            return foundParent;
        } else {
            return parent;
        }
    }

    private int findCurrentDepth(String line) {
        int indent = readIndent(line);
        int depth;
        if (indent % 4 == 0) {
            depth = indent / 4;
        } else {
            depth = (indent - (indent % 4)) / 4;
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
        in.close();
    }
}
