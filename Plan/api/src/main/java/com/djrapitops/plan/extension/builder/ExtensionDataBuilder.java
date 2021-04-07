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

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.table.Table;

import java.util.function.Supplier;

/**
 * Builder API for Extension data.
 * <p>
 * Requires Capability DATA_EXTENSION_BUILDER_API
 * <p>
 * Obtain an instance with {@link DataExtension#newExtensionDataBuilder()}
 * <p>
 * Used with {@link com.djrapitops.plan.extension.annotation.DataBuilderProvider}.
 * See documentation on how to use the API.
 *
 * @author AuroraLS3
 */
public interface ExtensionDataBuilder {

    /**
     * Creates a new {@link ValueBuilder} in order to use addValue methods.
     * <p>
     * If you need to use {@link com.djrapitops.plan.extension.annotation.InvalidateMethod} with built values,
     * lowercase 'text' and remove all whitespace. Example {@code valueBuilder("Times Jumped"); @InvalidateMethod("timesjumped")}
     *
     * @param text Text that should be displayed next to the value.
     * @return a new value builder.
     */
    ValueBuilder valueBuilder(String text);

    /**
     * Add a value.
     *
     * @param ofType    Class for type of the data, matches what Provider annotations want.
     * @param dataValue Use {@link ValueBuilder} to create one.
     * @param <T>       Type of the data.
     * @return This builder.
     */
    <T> ExtensionDataBuilder addValue(Class<T> ofType, DataValue<T> dataValue);

    /**
     * Compared to the other addValue method, this method allows you to use {@link com.djrapitops.plan.extension.NotReadyException} when building your data.
     *
     * @param ofType    Class for type of the data, matches what Provider annotations want.
     * @param dataValue Use {@link ValueBuilder} to create one.
     * @param <T>       Type of the data.
     * @return This builder.
     */
    <T> ExtensionDataBuilder addValue(Class<T> ofType, Supplier<DataValue<T>> dataValue);

    /**
     * Add a table.
     *
     * @param name  Name of the table, used in the database.
     * @param table Table built using {@link Table#builder()}
     * @param color Color of the table
     * @return This builder.
     */
    default ExtensionDataBuilder addTable(String name, Table table, Color color) {
        if (name == null) throw new IllegalArgumentException("'name' can not be null!");
        return addTable(name, table, color, null);
    }

    /**
     * Add a table to a specific tab.
     *
     * @param name  Name of the table, used in the database.
     * @param table Table built using {@link Table#builder()}
     * @param color Color of the table
     * @param tab   Name of the tab, remember to define {@link com.djrapitops.plan.extension.annotation.TabInfo}.
     * @return This builder.
     */
    default ExtensionDataBuilder addTable(String name, Table table, Color color, String tab) {
        return addValue(Table.class, valueBuilder(name)
                .showOnTab(tab)
                .buildTable(table, color));
    }

    /**
     * Adds all values and tables in another builder to this builder.
     *
     * @param builder Builder to combine with this one.
     */
    void addAll(ExtensionDataBuilder builder);
}
