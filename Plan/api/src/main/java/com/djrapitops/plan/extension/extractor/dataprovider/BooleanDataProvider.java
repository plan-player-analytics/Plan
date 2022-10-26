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

import com.djrapitops.plan.extension.annotation.BooleanProvider;
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;

import java.util.function.Supplier;

public class BooleanDataProvider extends AnnotationFullDataProvider<BooleanProvider, Boolean, Boolean> {

    public BooleanDataProvider(BooleanProvider provider, ExtensionMethod method) {
        super(provider, method, boolean.class, Boolean.class);
    }

    @Override
    public ValueBuilder getValueBuilder(ExtensionDataBuilder dataBuilder) {
        return super.getValueBuilder(dataBuilder)
                .hideFromUsers(provider);
    }

    @Override
    public DataValue<Boolean> addDataValueToBuilder(ValueBuilder builder, Supplier<Boolean> dataSupplier) {
        return builder.buildBooleanProvidingCondition(dataSupplier, provider.conditionName());
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
