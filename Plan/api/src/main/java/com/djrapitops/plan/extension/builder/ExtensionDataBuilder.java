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
package com.djrapitops.plan.extension.builder;

import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.table.Table;

import java.util.function.Supplier;

public interface ExtensionDataBuilder {

    ValueBuilder valueBuilder(String text);

    /**
     * Add a value to the builder.
     *
     * @param ofType    Class for type of the data, matches what Provider annotations want.
     * @param dataValue Use {@link ValueBuilder} to get one.
     * @param <T>       Type of the data.
     * @return This builder.
     */
    <T> ExtensionDataBuilder addValue(Class<T> ofType, DataValue<T> dataValue);

    /**
     * Compared to the other addValue method, this method allows you to use {@link com.djrapitops.plan.extension.NotReadyException} when building your data.
     *
     * @param ofType    Class for type of the data, matches what Provider annotations want.
     * @param dataValue Use {@link ValueBuilder} to get one.
     * @param <T>       Type of the data.
     * @return This builder.
     */
    <T> ExtensionDataBuilder addValue(Class<T> ofType, Supplier<DataValue<T>> dataValue);

    default ExtensionDataBuilder addTable(String name, Table table, Color color) {
        if (name == null) throw new IllegalArgumentException("'name' can not be null!");
        return addTable(name, table, color, null);
    }

    default ExtensionDataBuilder addTable(String name, Table table, Color color, String tab) {
        return addValue(Table.class, valueBuilder(name)
                .showOnTab(tab)
                .buildTable(table, color));
    }

    void addAll(ExtensionDataBuilder builder);
}
