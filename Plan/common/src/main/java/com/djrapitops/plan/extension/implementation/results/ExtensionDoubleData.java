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

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.extension.builder.DataValue;

/**
 * Represents double data returned by a DoubleProvider or PercentageProvider method.
 *
 * @author AuroraLS3
 */
// TODO differentiate between percentage and double data
public class ExtensionDoubleData implements DescribedExtensionData, DataValue<Double> {

    private final ExtensionDescription description;
    private final double value;

    public ExtensionDoubleData(ExtensionDescription description, double value) {
        this.description = description;
        this.value = value;
    }

    @Override
    public Double getValue() {
        return value;
    }

    public ExtensionDescription getDescription() {
        return description;
    }

    public String getFormattedValue(Formatter<Double> formatter) {
        return formatter.apply(value);
    }

    public double getRawValue() {
        return value;
    }
}