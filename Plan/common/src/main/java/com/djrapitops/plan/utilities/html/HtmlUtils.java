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
     * @deprecated Use {@link Html#swapColorCodesToSpan(String)} instead.
     * @return String with span elements.
     */
    @Deprecated
    public static String swapColorsToSpan(String string) {
        return Html.swapColorCodesToSpan(string);
    }
}
