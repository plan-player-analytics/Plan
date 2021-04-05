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
package com.djrapitops.plan.extension.implementation.builder;

import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.implementation.ProviderInformation;

import java.util.function.Supplier;

public class NumberDataValue implements DataValue<Long> {

    private final Long value;
    private final Supplier<Long> supplier;
    private final ProviderInformation information;

    public NumberDataValue(Long value, ProviderInformation information) {
        this(value, null, information);
    }

    public NumberDataValue(Supplier<Long> supplier, ProviderInformation information) {
        this(null, supplier, information);
    }

    private NumberDataValue(Long value, Supplier<Long> supplier, ProviderInformation information) {
        this.value = value;
        this.supplier = supplier;
        this.information = information;
    }

    @Override
    public Long getValue() {
        if (value != null) return value;
        if (supplier != null) return supplier.get();
        return null;
    }

    public ProviderInformation getInformation() {
        return information;
    }
}
