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
package com.djrapitops.plan.utilities.chat;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Formats chat messages in different ways.
 * <p>
 * <a href="https://hastebin.com/uziyajirag.avrasm">Original code</a>
 */
public class ChatFormatter {
    private static final int CENTER_PX = 154;
    private static final int MAX_PX = 260;

    private ChatFormatter() {
        // Hide utility class constructor
    }

    public static String leftPad(String message, int spaces) {
        StringBuilder returnMessage = new StringBuilder();

        String padding = StringUtils.leftPad("", spaces);
        int paddingWidth = getPxMessageWidth(padding);
        String[] parts = StringUtils.split(message, ' ');

        int lineWidth = paddingWidth;
        StringBuilder line = new StringBuilder(padding);

        for (String part : parts) {
            int width = getPxMessageWidth(part) + DefaultFontInfo.SPACE.getLength() + 1;
            line.append(part).append(' ');
            lineWidth += width;
            if (lineWidth > MAX_PX) {
                String finishedLine = StringUtils.chop(line.toString());
                returnMessage.append(finishedLine).append("\n");
                lineWidth = paddingWidth;
                line = new StringBuilder(padding);
            }
        }
        returnMessage.append(StringUtils.chop(line.toString()));
        return returnMessage.toString();
    }

    public static String columns(int columns, String[] lines, String separator) {
        StringBuilder returnMessage = new StringBuilder();
        List<String[]> table = new ArrayList<>();
        for (String line : lines) {
            table.add(StringUtils.split(line, separator, columns));
        }
        int[] biggestWidth = getBiggestWidthsForColumns(columns, table);

        for (String[] line : table) {
            StringBuilder lineBuilder = new StringBuilder();

            String currentStyle = "";
            for (int i = 0; i < line.length; i++) {
                int columnWidth = getPxMessageWidth(line[i]);
                int required = biggestWidth[i] + DefaultFontInfo.SPACE.getLength() + 1;
                lineBuilder.append(line[i]);
                currentStyle = getLastStyle(currentStyle + line[i]);
                compensate(required, columnWidth, lineBuilder, currentStyle);
            }
            returnMessage.append(lineBuilder.toString()).append("\n");
        }
        return returnMessage.toString();
    }

    private static int[] getBiggestWidthsForColumns(int columns, List<String[]> table) {
        int[] biggestWidth = new int[columns];
        for (String[] line : table) {
            for (int i = 0; i < line.length; i++) {
                int width = getPxMessageWidth(line[i]);
                if (biggestWidth[i] < width) {
                    biggestWidth[i] = width;
                }
            }
        }
        return biggestWidth;
    }

    // Checkstyle.OFF: CyclomaticComplexity

    public static String getLastStyle(String message) {
        boolean wasColorChar = false;
        char color = ' ';
        boolean k = false;
        boolean l = false;
        boolean m = false;
        boolean n = false;
        boolean o = false;
        for (char c : message.toCharArray()) {
            if (c == '\u00a7') { // §
                wasColorChar = true;
            } else if (wasColorChar) {
                switch (c) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case 'a':
                    case 'b':
                    case 'c':
                    case 'd':
                    case 'e':
                    case 'f':
                        color = c;
                        break;
                    case 'k':
                        k = true;
                        break;
                    case 'l':
                        l = true;
                        break;
                    case 'm':
                        m = true;
                        break;
                    case 'n':
                        n = true;
                        break;
                    case 'o':
                        o = true;
                        break;
                    case 'r':
                        color = ' ';
                        k = false;
                        l = false;
                        m = false;
                        n = false;
                        o = false;
                        break;
                    default:
                        break;
                }
            }
        }
        return (color == ' ' ? "§" + color : "") + (k ? "§k" : "") + (l ? "§l" : "") + (m ? "§m" : "") + (n ? "§n" : "") + (o ? "§o" : "");
    }

    // Checkstyle.ON: CyclomaticComplexity

    public static String center(String message) {
        if (message == null) return null;
        if (message.isEmpty()) return "";

        int messagePxWidth = getPxMessageWidth(message);

        int halfOfWidth = messagePxWidth / 2;
        int toCompensate = CENTER_PX - halfOfWidth;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        compensate(toCompensate, compensated, sb, "");
        return sb.append(message).toString();
    }

    public static void compensate(int toCompensate, int compensated, StringBuilder builder, String currentStyle) {
        int space = DefaultFontInfo.SPACE.getLength() + 1;
        int boldSpace = DefaultFontInfo.SPACE.getBoldLength() + 1;
        while (compensated < toCompensate) {
            int left = toCompensate - compensated;
            if (left % 6 == 0) {
                builder.append("§l");
                for (int i = 0; i < left / 6; i++) {
                    builder.append(" ");
                    compensated += boldSpace;
                }
                builder.append("§r").append(currentStyle);
            } else {
                builder.append(" ");
                compensated += space;
            }
        }
    }

    public static int getPxMessageWidth(String message) {
        int messagePxSize = 0;
        boolean wasColorChar = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '\u00a7') { // §
                wasColorChar = true;
            } else if (wasColorChar) {
                wasColorChar = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }
        return messagePxSize;
    }
}
