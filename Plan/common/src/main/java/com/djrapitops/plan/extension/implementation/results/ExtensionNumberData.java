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
import com.djrapitops.plan.extension.FormatType;

/**
 * Represents double data returned by a DoubleProvider or PercentageProvider method.
 *
 * @author AuroraLS3
 */
public class ExtensionNumberData implements DescribedExtensionData {

    private final ExtensionDescriptive descriptive;
    private final FormatType formatType;
    private final long value;

    public ExtensionNumberData(ExtensionDescriptive descriptive, FormatType formatType, long value) {
        this.descriptive = descriptive;
        this.formatType = formatType;
        this.value = value;
    }

    public ExtensionDescriptive getDescriptive() {
        return descriptive;
    }

    public FormatType getFormatType() {
        return formatType;
    }

    public String getFormattedValue(Formatter<Long> formatter) {
        return formatter.apply(value);
    }

    public long getRawValue() {
        return value;
    }
}