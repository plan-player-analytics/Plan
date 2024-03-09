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
 * Enum to determine font-awesome icon family.
 *
 * @author AuroraLS3
 */
public enum Family {
    /**
     * 'fas' (solid) Font awesome family.
     */
    SOLID,
    /**
     * 'far' (regular) Font awesome family.
     */
    REGULAR,
    /**
     * 'fab' (brand) Font awesome family.
     */
    BRAND;

    /**
     * Get a family by the enum name without exception.
     *
     * @param name Family#name()
     * @return Optional if the family is found by that name, empty if not found.
     */
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
}
