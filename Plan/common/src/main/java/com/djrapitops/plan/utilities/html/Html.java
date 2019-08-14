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
package com.djrapitops.plan.utilities.html;

import org.apache.commons.text.StringSubstitutor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rsl1122
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

    /**
     * @deprecated Use com.djrapitops.plan.utilities.html.icon.Icon instead
     */
    @Deprecated
    FONT_AWESOME_ICON("<i class=\"fa fa-${0}\"></i>"),
    /**
     * @deprecated Use com.djrapitops.plan.utilities.html.icon.Icon instead
     */
    @Deprecated
    FA_COLORED_ICON("<i class=\"col-${0} fa fa-${1}\"></i>"),
    SPAN("${0}</span>"),
    BUTTON("<a class=\"button\" href=\"${0}\">${1}</a>"),
    BUTTON_CLASS("class=\"button\""),
    LINK("<a class=\"link\" href=\"${0}\">${1}</a>"),
    LINK_A("<a href=\"${0}\">${1}</a>"),
    LINK_TOOLTIP("<a title=\"${2}\" class=\"link\" href=\"${0}\">${1}</a>"),
    LINK_EXTERNAL("<a class=\"link\" target=\"_blank\" href=\"${0}\">${1}</a>"),
    LINK_CLASS("class=\"link\""),
    IMG("<img src=\"${0}\">"),

    PARAGRAPH("<p>${0}</p>"),
    HEADER("<h1>${0}</h1>"),
    HEADER_2("<h2>${0}</h2>"),

    DIV_W_CLASS("<div class=\"${0}\">${1}</div>"),
    DIV_W_CLASS_STYLE("<div class=\"${0}\" style=\"${1}\">${2}</div>"),

    ROW("<div class=\"row\">${0}</div>"),
    CARD("<div class=\"card\">${0}</div>"),
    BODY("<div class=\"card-body\">${0}</div>"),
    PANEL("<div class=\"panel panel-default\">${0}</div>"),
    PANEL_BODY("<div class=\"panel-body\">${0}</div>"),
    HELP_BUBBLE("<div class=\"col-xs-6 col-sm-6 col-lg-6\"><a href=\"javascript:void(0)\" class=\"help material-icons pull-right\" data-trigger=\"focus\" data-toggle=\"popover\" data-placement=\"left\" data-container=\"body\" data-html=\"true\" data-original-title=\"${0}\" data-content=\"${1}\">help_outline</a></div>"),

    TABLE_END("</tbody></table>"),
    TABLE("<table class=\"table table-striped\">"),
    TABLE_SCROLL("<div class=\"scrollbar\"><table class=\"table table-striped\">"),
    TABLE_JQUERY("<table class=\"table table-bordered table-striped table-hover ${0} dataTable\">"),
    TABLE_COLORED("<table class=\"bg-${0} table table-striped\">"),
    TABLE_HEAD("<thead>${0}</thead>"),
    TABLE_BODY("<tbody>${0}</tbody>"),
    TABLE_START_2("<table class=\"table table-striped\"><thead><tr><th>${0}</th><th>${1}</th></tr></thead><tbody>"),
    TABLE_START_3("<table class=\"table table-striped\"><thead><tr><th>${0}</th><th>${1}</th><th>${2}</th></tr></thead><tbody>"),
    TABLE_START_4("<table class=\"table table-striped\"><thead><tr><th>${0}</th><th>${1}</th><th>${2}</th><th>${3}</th></tr></thead><tbody>"),
    TABLELINE_2("<tr><td><b>${0}</b></td><td>${1}</td></tr>"),
    TABLELINE_3("<tr><td><b>${0}</b></td><td>${1}</td><td>${2}</td></tr>"),
    TABLELINE_4("<tr><td><b>${0}</b></td><td>${1}</td><td>${2}</td><td>${3}</td></tr>"),
    TABLELINE_PLAYERS("<tr><td>${0}</td><td>${1}</td><td data-order=\"${2}\">${3}</td><td>${4}</td><td data-order=\"${5}\">${6}</td>" + "<td data-order=\"${7}\">${8}</td><td>${9}</td></tr>"),
    TABLELINE_PLAYERS_PLAYERS_PAGE("<tr><td>${0}</td><td data-order=\"${1}\">${2}</td><td>${3}</td><td>${4}</td>" + "<td>${5}</td><td>${6}</td></tr>"),
    TABLELINE_3_CUSTOMKEY("<tr><td sorttable_customkey=\"${0}\">${1}</td><td sorttable_customkey=\"${2}\">${3}</td><td sorttable_customkey=\"${4}\">${5}</td></tr>"),
    TABLELINE_3_CUSTOMKEY_1("<tr><td sorttable_customkey=\"${0}\">${1}</td><td>${2}</td><td>${3}</td></tr>");

    private final String html;

    Html(String html) {
        this.html = html;
    }

    /**
     * @return The HTML String
     */
    public String parse() {
        return html;
    }

    /**
     * Changes Minecraft color codes to HTML span elements with correct color class assignments.
     *
     * @param string String to replace Minecraft color codes from
     * @return String with span elements.
     */
    public static String swapColorCodesToSpan(String string) {
        Html[] replacer = new Html[]{
                Html.COLOR_0, Html.COLOR_1, Html.COLOR_2, Html.COLOR_3,
                Html.COLOR_4, Html.COLOR_5, Html.COLOR_6, Html.COLOR_7,
                Html.COLOR_8, Html.COLOR_9, Html.COLOR_A, Html.COLOR_B,
                Html.COLOR_C, Html.COLOR_D, Html.COLOR_E, Html.COLOR_F
        };
        Map<Character, String> colorMap = new HashMap<>();

        for (Html html : replacer) {
            colorMap.put(Character.toLowerCase(html.name().charAt(6)), html.parse());
            colorMap.put('k', "");
            colorMap.put('l', "");
            colorMap.put('m', "");
            colorMap.put('n', "");
            colorMap.put('o', "");
        }

        StringBuilder result = new StringBuilder(string.length());
        String[] split = string.split("§");
        // Skip first part if it does not start with §
        boolean skipFirst = !string.startsWith("§");

        int placedSpans = 0;
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
            if (colorChar == 'r') {
                appendEndTags(result, placedSpans);
                placedSpans = 0; // Colors were reset
                result.append(part.substring(1));
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

    private static void appendEndTags(StringBuilder result, int placedSpans) {
        for (int i = 0; i < placedSpans; i++) {
            result.append("</span>");
        }
    }

    /**
     * @param replacements The replacement Strings
     * @return The parsed HTML String
     */
    public String parse(Serializable... replacements) {
        Map<String, Serializable> replaceMap = new HashMap<>();

        for (int i = 0; i < replacements.length; i++) {
            replaceMap.put(String.valueOf(i), replacements[i]);
        }

        StringSubstitutor sub = new StringSubstitutor(replaceMap);
        sub.setEnableSubstitutionInVariables(false);
        return sub.replace(html);
    }
}
