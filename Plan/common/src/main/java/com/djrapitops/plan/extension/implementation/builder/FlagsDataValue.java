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

import com.djrapitops.plan.extension.extractor.ExtensionMethod.ParameterType;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.statistic.Flags;

import java.util.function.Supplier;

public class FlagsDataValue extends BuiltDataValue<Flags> {
    private final ParameterType parameterType;

    public FlagsDataValue(Flags value, ProviderInformation information, ParameterType parameterType) {
        super(value, information);
        this.parameterType = parameterType;
    }

    public FlagsDataValue(Supplier<Flags> supplier, ProviderInformation information, ParameterType parameterType) {
        super(supplier, information);
        this.parameterType = parameterType;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }
}