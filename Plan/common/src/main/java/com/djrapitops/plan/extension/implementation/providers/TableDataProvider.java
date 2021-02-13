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
package com.djrapitops.plan.extension.implementation.providers;

import com.djrapitops.plan.extension.annotation.Conditional;
import com.djrapitops.plan.extension.annotation.TableProvider;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.table.Table;

import java.lang.reflect.Method;

/**
 * Contains code that acts on {@link TableProvider} annotations.
 * <p>
 * Please note that not all {@link ProviderInformation} is present in this annotation.
 *
 * @author AuroraLS3
 */
public class TableDataProvider {

    private TableDataProvider() {
        // Static method class
    }

    public static void placeToDataProviders(
            DataProviders dataProviders, Method method, TableProvider annotation,
            Conditional condition, String tab, String pluginName
    ) {
        ProviderInformation information = ProviderInformation.builder(pluginName)
                .setName(method.getName())
                .setPriority(0)
                .setCondition(condition)
                .setTab(tab)
                .setTableColor(annotation.tableColor())
                .build();

        MethodWrapper<Table> methodWrapper = new MethodWrapper<>(method, Table.class);
        dataProviders.put(new DataProvider<>(information, methodWrapper));
    }

}