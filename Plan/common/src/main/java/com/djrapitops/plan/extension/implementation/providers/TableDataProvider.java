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
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.table.Table;

import java.lang.reflect.Method;

/**
 * Represents a DataExtension API method annotated with {@link com.djrapitops.plan.extension.annotation.TableProvider} annotation.
 * <p>
 * Used to obtain data to place in the database.
 * <p>
 * Please note that not all {@link ProviderInformation} is present.
 *
 * @author Rsl1122
 */
public class TableDataProvider extends DataProvider<Table> {

    private final Color tableColor;

    private TableDataProvider(ProviderInformation providerInformation, MethodWrapper<Table> methodWrapper, Color tableColor) {
        super(providerInformation, methodWrapper);

        this.tableColor = tableColor;
    }

    public static void placeToDataProviders(
            DataProviders dataProviders, Method method, TableProvider annotation,
            Conditional condition, String tab, String pluginName
    ) {
        MethodWrapper<Table> methodWrapper = new MethodWrapper<>(method, Table.class);

        ProviderInformation providerInformation = new ProviderInformation(
                pluginName, method.getName(), null, null, null, 0, tab, condition
        );

        dataProviders.put(new TableDataProvider(providerInformation, methodWrapper, annotation.tableColor()));
    }

    public static Color getTableColor(DataProvider<Table> provider) {
        if (provider instanceof TableDataProvider) {
            return ((TableDataProvider) provider).getTableColor();
        }
        return Color.NONE;
    }

    public Color getTableColor() {
        return tableColor;
    }
}