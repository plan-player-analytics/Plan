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

import com.djrapitops.plan.extension.annotation.GroupProvider;
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;

import java.util.function.Supplier;

public class GroupDataProvider extends AnnotationTextAndIconProvider<GroupProvider, String[], String[]> {

    public GroupDataProvider(GroupProvider provider, ExtensionMethod method) {
        super(provider, method, String[].class, String[].class);
    }

    @Override
    public String getBuilderName() {
        return text();
    }

    @Override
    public DataValue<String[]> addDataValueToBuilder(ValueBuilder builder, Supplier<String[]> dataSupplier) {
        return builder.buildGroup(dataSupplier);
    }

    @Override
    public String text() {
        return provider.text();
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
        return Color.NONE;
    }
}
