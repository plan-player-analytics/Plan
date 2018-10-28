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

/**
 * @author Rsl1122
 */
public class HtmlUtils {

    /**
     * Constructor used to hide the public constructor
     */
    private HtmlUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Attempts to remove XSS components.
     *
     * @param string String to remove XSS components from
     * @return String but with the components removed
     */
    public static String removeXSS(String string) {
        return string.replace("<!--", "").replace("-->", "").replace("</script>", "").replace("<script>", "");
    }

    /**
     * Changes Minecraft color codes to HTML span elements with correct color class assignments.
     *
     * @param string String to replace Minecraft color codes from
     * @return String with span elements.
     */
    public static String swapColorsToSpan(String string) {
        Html[] replacer = new Html[]{Html.COLOR_0, Html.COLOR_1, Html.COLOR_2, Html.COLOR_3,
                Html.COLOR_4, Html.COLOR_5, Html.COLOR_6, Html.COLOR_7, Html.COLOR_8, Html.COLOR_9,
                Html.COLOR_A, Html.COLOR_B, Html.COLOR_C, Html.COLOR_D, Html.COLOR_E, Html.COLOR_F};

        for (Html html : replacer) {
            string = string.replace("§" + Character.toLowerCase(html.name().charAt(6)), html.parse());
        }

        int spans = string.split("<span").length - 1;
        for (int i = 0; i < spans; i++) {
            string = Html.SPAN.parse(string);
        }

        return string.replace("§r", "").replace("§l", "").replace("§m", "").replace("§n", "").replace("§o", "").replace("§k", "");
    }
}
