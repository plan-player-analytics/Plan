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

import com.djrapitops.plan.extension.annotation.NumberProvider;
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;

import java.util.function.Supplier;

public class NumberDataProvider extends AnnotationFullDataProvider<NumberProvider, Long, Long> {

    public NumberDataProvider(NumberProvider provider, ExtensionMethod method) {
        super(provider, method, long.class, Long.class);
    }

    @Override
    public ValueBuilder getValueBuilder(ExtensionDataBuilder dataBuilder) {
        return super.getValueBuilder(dataBuilder)
                .format(provider.format());
    }

    @Override
    public DataValue<Long> addDataValueToBuilder(ValueBuilder builder, Supplier<Long> dataSupplier) {
        return builder.buildNumber(dataSupplier);
    }

    @Override
    public String iconName() {
        return provider.iconName();
    }

    @Override
    public Family iconFamily() {
        return provider.iconFamily();
    }

    @Override
    public Color iconColor() {
        return provider.iconColor();
    }

    @Override
    public String text() {
        return provider.text();
    }

    @Override
    public String description() {
        return provider.description();
    }

    @Override
    public int priority() {
        return provider.priority();
    }

    @Override
    public boolean showInPlayerTable() {
        return provider.showInPlayerTable();
    }
}
