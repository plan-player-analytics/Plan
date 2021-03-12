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

import com.djrapitops.plan.extension.builder.DataValue;

/**
 * Represents boolean data returned by a BooleanProvider method.
 *
 * @author AuroraLS3
 */
public class ExtensionBooleanData implements DescribedExtensionData, DataValue<Boolean> {

    private final ExtensionDescription description;
    private final boolean value;

    public ExtensionBooleanData(ExtensionDescription description, boolean value) {
        this.description = description;
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    public ExtensionDescription getDescription() {
        return description;
    }

    public String getFormattedValue() {
        return value ? "Yes" : "No";
    }
}