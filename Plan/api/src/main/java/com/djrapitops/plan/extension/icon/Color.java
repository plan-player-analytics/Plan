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
package com.djrapitops.plan.extension.icon;

import java.util.Optional;

/**
 * Enum to determine what color to use for some element.
 *
 * @author AuroraLS3
 */
public enum Color {

    /**
     * Red color set in the theme.
     */
    RED,
    /**
     * Pink color set in the theme.
     */
    PINK,
    /**
     * Purple color set in the theme.
     */
    PURPLE,
    /**
     * Deep purple color set in the theme.
     */
    DEEP_PURPLE,
    /**
     * Indigo color set in the theme.
     */
    INDIGO,
    /**
     * Blue color set in the theme.
     */
    BLUE,
    /**
     * Light blue color set in the theme.
     */
    LIGHT_BLUE,
    /**
     * Cyan color set in the theme.
     */
    CYAN,
    /**
     * Teal color set in the theme.
     */
    TEAL,
    /**
     * Green color set in the theme.
     */
    GREEN,
    /**
     * Light green color set in the theme.
     */
    LIGHT_GREEN,
    /**
     * Lime color set in the theme.
     */
    LIME,
    /**
     * Yellow color set in the theme.
     */
    YELLOW,
    /**
     * Amber color set in the theme.
     */
    AMBER,
    /**
     * Orange color set in the theme.
     */
    ORANGE,
    /**
     * Deep orange color set in the theme.
     */
    DEEP_ORANGE,
    /**
     * Brown color set in the theme.
     */
    BROWN,
    /**
     * Grey color set in the theme.
     */
    GREY,
    /**
     * Blue grey color set in the theme.
     */
    BLUE_GREY,
    /**
     * Black color set in the theme.
     */
    BLACK,
    /**
     * No color that renders as text color.
     */
    NONE;

    /**
     * Get a color by the enum name without exception.
     *
     * @param name Color#name()
     * @return Optional if the color is found by that name, empty if not found.
     */
    public static Optional<Color> getByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
