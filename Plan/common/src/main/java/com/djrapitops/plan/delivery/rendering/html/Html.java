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
package com.djrapitops.plan.delivery.rendering.html;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.TextStringBuilder;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author AuroraLS3
 */
public enum Html {

    COLOR_0("<span class=\"black\">"),
    COLOR_1("<span class=\"darkblue\">"),
    COLOR_2("<span class=\"darkgreen\">"),
    COLOR_3("<span class=\"darkaqua\">"),
    COLOR_4("<span class=\"darkred\">"),
    COLOR_5("<span class=\"darkpurple\">"),
    COLOR_6("<span class=\"gold\">"),
    COLOR_7("<span class=\"gray\">"),
    COLOR_8("<span class=\"darkgray\">"),
    COLOR_9("<span class=\"blue\">"),
    COLOR_A("<span class=\"green\">"),
    COLOR_B("<span class=\"aqua\">"),
    COLOR_C("<span class=\"red\">"),
    COLOR_D("<span class=\"pink\">"),
    COLOR_E("<span class=\"yellow\">"),
    COLOR_F("<span class=\"white\">"),

    SPAN("${0}</span>"),
    LINK("<a class=\"link\" href=\"${0}\">${1}</a>"),
    LINK_EXTERNAL("<a class=\"link\" rel=\"noopener noreferrer\" target=\"_blank\" href=\"${0}\">${1}</a>"),

    BACK_BUTTON_NETWORK("<a class=\"btn bg-plan btn-icon-split\" href=\"../network\">" +
            "<span class=\"icon text-white-50\">" +
            "<i class=\"fas fa-fw fa-arrow-left\"></i><i class=\"fas fa-fw fa-cloud\"></i>" +
            "</span>" +
            "<span class=\"text\">Network page</span>" +
            "</a>"),
    BACK_BUTTON_SERVER("<a class=\"btn bg-plan btn-icon-split\" href=\"../server/\">" +
            "<span class=\"icon text-white-50\">" +
            "<i class=\"fas fa-fw fa-arrow-left\"></i><i class=\"fas fa-fw fa-server\"></i>" +
            "</span>" +
            "<span class=\"text\">Server page</span>" +
            "</a>");

    private final String html;

    Html(String html) {
        this.html = html;
    }

    /**
     * Changes Minecraft color codes to HTML span elements with correct color class assignments.
     *
     * @param string String to replace Minecraft color codes from
     * @return String with span elements.
     */
    public static String swapColorCodesToSpan(String string) {
        return swapColorCodesToSpan(string, string.contains("&sect;") ? "&sect;" : "§");
    }

    private static String swapColorCodesToSpan(String string, String splitWith) {
        if (string == null) return null;
        if (!string.contains(splitWith)) return string;

        Html[] replacer = new Html[]{
                Html.COLOR_0, Html.COLOR_1, Html.COLOR_2, Html.COLOR_3,
                Html.COLOR_4, Html.COLOR_5, Html.COLOR_6, Html.COLOR_7,
                Html.COLOR_8, Html.COLOR_9, Html.COLOR_A, Html.COLOR_B,
                Html.COLOR_C, Html.COLOR_D, Html.COLOR_E, Html.COLOR_F
        };
        Map<Character, String> colorMap = new HashMap<>();

        for (Html html : replacer) {
            colorMap.put(Character.toLowerCase(html.name().charAt(6)), html.create());
            colorMap.put('k', "");
            colorMap.put('l', "");
            colorMap.put('m', "");
            colorMap.put('n', "");
            colorMap.put('o', "");
        }

        StringBuilder result = new StringBuilder(string.length());
        String[] split = string.split(splitWith);
        // Skip first part if it does not start with §
        boolean skipFirst = !string.startsWith(splitWith);

        int placedSpans = 0;
        int hexNumbersLeft = 0;

        for (String part : split) {
            if (part.isEmpty()) {
                continue;
            }
            if (skipFirst) {
                result.append(part);
                skipFirst = false;
                continue;
            }

            char colorChar = part.charAt(0);
            // Deal with hex colors
            if (hexNumbersLeft > 1) {
                result.append(colorChar);
                hexNumbersLeft--;
                continue;
            } else if (hexNumbersLeft == 1) {
                result.append(colorChar).append(";\">").append(part.substring(1));
                hexNumbersLeft--;
                continue;
            }

            if (colorChar == 'r') {
                appendEndTags(result, placedSpans);
                placedSpans = 0; // Colors were reset
                result.append(part.substring(1));
                continue;
            }

            // Deal with hex colors
            if (colorChar == 'x') {
                result.append("<span style=\"color: #");
                hexNumbersLeft = 6;
                placedSpans++;
                continue;
            }

            String replacement = colorMap.get(colorChar);
            if (replacement != null) {
                result.append(replacement).append(part.substring(1));

                if (!replacement.isEmpty()) {
                    placedSpans++;
                }
            } else {
                result.append(part);
            }
        }

        appendEndTags(result, placedSpans);

        return result.toString();
    }

    public static String separateWithDots(String... elements) {
        TextStringBuilder builder = new TextStringBuilder();
        builder.appendWithSeparators(elements, " &#x2022; ");
        return builder.toString();
    }

    /**
     * @return The HTML String
     */
    public String create() {
        return html;
    }

    private static void appendEndTags(StringBuilder result, int placedSpans) {
        for (int i = 0; i < placedSpans; i++) {
            result.append("</span>");
        }
    }

    /**
     * @param replacements The replacement Strings
     * @return The HTML String
     */
    public String create(Serializable... replacements) {
        Map<String, Serializable> replaceMap = new HashMap<>();

        for (int i = 0; i < replacements.length; i++) {
            replaceMap.put(String.valueOf(i), replacements[i]);
        }

        StringSubstitutor sub = new StringSubstitutor(replaceMap);
        sub.setEnableSubstitutionInVariables(false);
        return sub.replace(html);
    }

    public static String encodeToURL(String string) {
        try {
            return StringUtils.replace(
                    URLEncoder.encode(string, "UTF-8"),
                    "+", "%20" // Encoding replaces spaces with +
            );
        } catch (UnsupportedEncodingException e) {
            return string;
        }
    }
}
