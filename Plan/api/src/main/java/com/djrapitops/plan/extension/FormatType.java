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
package com.djrapitops.plan.extension;

import java.util.Optional;

/**
 * Enum for determining additional formatter for a value given by a {@link com.djrapitops.plan.extension.annotation.NumberProvider}.
 *
 * @author AuroraLS3
 */
public enum FormatType {

    /**
     * Formats a long value (Epoch ms) to a readable timestamp, year is important.
     */
    DATE_YEAR,
    /**
     * Formats a long value (Epoch ms) to a readable timestamp, second is important.
     */
    DATE_SECOND,
    /**
     * Formats a long value (ms) to a readable format.
     */
    TIME_MILLISECONDS,
    /**
     * Applies no formatting to the value.
     */
    NONE;

    /**
     * Get a format type by the enum name without exception.
     *
     * @param name FormatType#name()
     * @return Optional if the format type is found by that name, empty if not found.
     */
    public static Optional<FormatType> getByName(String name) {
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
