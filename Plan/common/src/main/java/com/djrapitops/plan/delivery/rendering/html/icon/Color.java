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
package com.djrapitops.plan.delivery.rendering.html.icon;

import org.apache.commons.lang3.Strings;

import java.util.Optional;

public enum Color {
    RED("col-plugin-red"),
    PINK("col-plugin-pink"),
    PURPLE("col-plugin-purple"),
    DEEP_PURPLE("col-plugin-deep-purple"),
    INDIGO("col-plugin-indigo"),
    BLUE("col-plugin-blue"),
    LIGHT_BLUE("col-plugin-light-blue"),
    CYAN("col-plugin-cyan"),
    TEAL("col-plugin-teal"),
    GREEN("col-plugin-green"),
    LIGHT_GREEN("col-plugin-light-green"),
    LIME("col-plugin-lime"),
    YELLOW("col-plugin-yellow"),
    AMBER("col-plugin-amber"),
    ORANGE("col-plugin-orange"),
    DEEP_ORANGE("col-plugin-deep-orange"),
    BROWN("col-plugin-brown"),
    GREY("col-plugin-grey"),
    BLUE_GREY("col-plugin-blue-grey"),
    BLACK("col-plugin-black"),
    NONE("");

    private final String htmlClass;

    Color(String htmlClass) {
        this.htmlClass = htmlClass;
    }

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

    public String getHtmlClass() {
        return htmlClass;
    }

    public String getBackgroundColorClass() {
        return Strings.CS.replace(htmlClass, "col-plugin-", "bg-plugin-");
    }
}
