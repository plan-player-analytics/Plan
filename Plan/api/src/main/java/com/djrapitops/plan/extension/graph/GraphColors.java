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
package com.djrapitops.plan.extension.graph;

/**
 * Static variable class for always available graph colors that match Plan themes.
 * <p>
 * These match {@link com.djrapitops.plan.extension.icon.Color}.
 * <p>
 * To be used with {@link SeriesMetadata#getHexColor()}
 *
 * @author AuroraLS3
 */
public class GraphColors {
    public static String RED = "var(--color-red)";
    public static String PINK = "var(--color-pink)";
    public static String PURPLE = "var(--color-purple)";
    public static String DEEP_PURPLE = "var(--color-deep-purple)";
    public static String INDIGO = "var(--color-indigo)";
    public static String BLUE = "var(--color-blue)";
    public static String LIGHT_BLUE = "var(--color-light-blue)";
    public static String CYAN = "var(--color-cyan)";
    public static String TEAL = "var(--color-teal)";
    public static String GREEN = "var(--color-green)";
    public static String LIGHT_GREEN = "var(--color-light-green)";
    public static String LIME = "var(--color-lime)";
    public static String YELLOW = "var(--color-yellow)";
    public static String AMBER = "var(--color-amber)";
    public static String ORANGE = "var(--color-orange)";
    public static String DEEP_ORANGE = "var(--color-deep-orange)";
    public static String BROWN = "var(--color-brown)";
    public static String GREY = "var(--color-grey)";
    public static String BLUE_GREY = "var(--color-blue-grey)";
    public static String BLACK = "var(--color-black)";

    private GraphColors() {
        /* Static variable class */
    }
}
