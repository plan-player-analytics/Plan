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
package com.djrapitops.plan.extension.implementation.results;

import com.djrapitops.plan.delivery.rendering.html.Html;

import java.util.Map;

/**
 * Represents double data returned by a DoubleProvider or PercentageProvider method.
 *
 * @author AuroraLS3
 */
public class ExtensionStringData implements DescribedExtensionData {

    private final ExtensionDescription description;
    private final boolean playerName;
    private String value;

    public ExtensionStringData(ExtensionDescription description, boolean playerName, String value) {
        this.description = description;
        this.playerName = playerName;
        this.value = value;
    }

    public static ExtensionStringData regularString(ExtensionDescription description, String value) {
        return new ExtensionStringData(description, false, value);
    }

    public static ExtensionStringData playerName(ExtensionDescription description, String value) {
        return new ExtensionStringData(description, true, value);
    }

    public ExtensionDescription getDescription() {
        return description;
    }

    public boolean isPlayerName() {
        return playerName;
    }

    public String getValue() {
        return value;
    }

    public Object getFormattedValue() {
        if (playerName) {
            return Map.of(
                    "link", "/player/" + Html.encodeToURL(value),
                    "text", value
            );
        } else {
            return value;
        }
    }

    ExtensionStringData concatenate(ExtensionStringData other) {
        value += ", " + other.value;
        return this;
    }
}