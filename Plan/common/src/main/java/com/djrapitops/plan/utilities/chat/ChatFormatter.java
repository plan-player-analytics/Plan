package com.djrapitops.plan.utilities.chat;

import org.apache.commons.lang3.StringUtils;

/**
 * Formats chat messages in different ways.
 * <p>
 * Original code: https://hastebin.com/uziyajirag.avrasm
 */
public class ChatFormatter {
    private final static int CENTER_PX = 154;
    private final static int MAX_PX = 250;

    public static String indent(int spaces, String message) {
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
        returnMessage.append(line.toString().trim());
        return returnMessage.toString();
    }

    public static String center(String message) {
        if (message == null) return null;
        if (message.isEmpty()) return "";

        int messagePxWidth = getPxMessageWidth(message);

        int halfOfWidth = messagePxWidth / 2;
        int toCompensate = CENTER_PX - halfOfWidth;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb.toString() + message;
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
