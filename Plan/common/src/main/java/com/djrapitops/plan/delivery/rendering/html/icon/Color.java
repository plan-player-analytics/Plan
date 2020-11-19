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

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public enum Color {
    RED("col-red"),
    PINK("col-pink"),
    PURPLE("col-purple"),
    DEEP_PURPLE("col-deep-purple"),
    INDIGO("col-indigo"),
    BLUE("col-blue"),
    LIGHT_BLUE("col-light-blue"),
    CYAN("col-cyan"),
    TEAL("col-teal"),
    GREEN("col-green"),
    LIGHT_GREEN("col-light-green"),
    LIME("col-lime"),
    YELLOW("col-yellow"),
    AMBER("col-amber"),
    ORANGE("col-orange"),
    DEEP_ORANGE("col-deep-orange"),
    BROWN("col-brown"),
    GREY("col-grey"),
    BLUE_GREY("col-blue-grey"),
    BLACK("col-black"),
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
        return StringUtils.replace(htmlClass, "col-", "bg-");
    }
}
