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
package com.djrapitops.plan.extension.extractor.dataprovider;

import com.djrapitops.plan.extension.annotation.DataBuilderProvider;
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;

import java.util.function.Supplier;

public class DataBuilderDataProvider extends AnnotationDataProvider<DataBuilderProvider, ExtensionDataBuilder, ExtensionDataBuilder> {

    public DataBuilderDataProvider(DataBuilderProvider provider, ExtensionMethod method) {
        super(provider, method, ExtensionDataBuilder.class, null);
    }

    @Override
    public DataValue<ExtensionDataBuilder> addDataValueToBuilder(ValueBuilder builder, Supplier<ExtensionDataBuilder> dataSupplier) {
        throw new IllegalStateException("addDataValueToBuilder is not suppose to be called on DataBuilderDataProvider");
    }
}
