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
package com.djrapitops.plan.utilities.html.icon;

import java.util.Optional;

/**
 * @deprecated This Class exists to keep plugins that used PluginData from breaking.
 */
@Deprecated(since = "5.0")
public enum Family {
    SOLID(" fa fa-", "\"></i>"),
    REGULAR(" far fa-", "\"></i>"),
    BRAND(" fab fa-", "\"></i>"),
    @Deprecated
    LINE(" material-icons\">", "</i>");

    private final String middle;
    private final String suffix;

    Family(String middle, String suffix) {
        this.middle = middle;
        this.suffix = suffix;
    }

    /**
     * @deprecated This Class exists to keep plugins that used PluginData from breaking.
     */
    @Deprecated
    public static Optional<Family> getByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * @deprecated This Class exists to keep plugins that used PluginData from breaking.
     */
    @Deprecated
    public String appendAround(String color, String name) {
        return "<i class=\"" + color + middle + name + suffix;
    }
}
