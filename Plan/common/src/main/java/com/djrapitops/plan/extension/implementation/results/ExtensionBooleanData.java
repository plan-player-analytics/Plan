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

/**
 * Represents boolean data returned by a BooleanProvider method.
 *
 * @author AuroraLS3
 */
public class ExtensionBooleanData implements DescribedExtensionData {

    private final ExtensionDescriptive descriptive;
    private final boolean value;

    public ExtensionBooleanData(ExtensionDescriptive descriptive, boolean value) {
        this.descriptive = descriptive;
        this.value = value;
    }

    public ExtensionDescriptive getDescriptive() {
        return descriptive;
    }

    public String getFormattedValue() {
        return value ? "Yes" : "No";
    }
}