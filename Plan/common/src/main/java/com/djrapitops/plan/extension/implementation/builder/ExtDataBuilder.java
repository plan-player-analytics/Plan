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

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.NotReadyException;
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.table.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ExtDataBuilder implements ExtensionDataBuilder {

    private final List<ClassValuePair> values;
    private final List<TabNameTablePair> tables;
    private final DataExtension extension;

    public ExtDataBuilder(DataExtension extension) {
        this.extension = extension;
        values = new ArrayList<>();
        tables = new ArrayList<>();
    }

    @Override
    public ValueBuilder valueBuilder(String text) {
        return new ExtValueBuilder(text, extension);
    }

    @Override
    public <T> ExtensionDataBuilder addValue(Class<T> ofType, DataValue<T> dataValue) {
        values.add(new ClassValuePair(ofType, dataValue));
        return this;
    }

    @Override
    public <T> ExtensionDataBuilder addValue(Class<T> ofType, Supplier<DataValue<T>> dataValue) {
        try {
            values.add(new ClassValuePair(ofType, dataValue.get()));
        } catch (NotReadyException ignored) {
            // This exception is ignored by default to allow throwing errors inside the lambda to keep code clean.
        }
        return this;
    }

    @Override
    public ExtensionDataBuilder addTable(Table table, String tab) {
        // TODO ProviderInformation instead.
        tables.add(new TabNameTablePair(tab, table));
        return this;
    }

    public List<ClassValuePair> getValues() {
        return values;
    }

    public List<TabNameTablePair> getTables() {
        return tables;
    }

    public static final class ClassValuePair {
        private final Class<?> type;
        private final DataValue<?> value;

        public <T> ClassValuePair(Class<T> type, DataValue<T> value) {
            this.type = type;
            this.value = value;
        }

        public <T> Optional<DataValue<T>> getValue(Class<T> ofType) {
            if (type.equals(ofType)) {
                return Optional.ofNullable((DataValue<T>) value);
            }
            return Optional.empty();
        }
    }

    public static final class TabNameTablePair {
        private final String tabName;
        private final Table table;

        public TabNameTablePair(String tabName, Table table) {
            this.tabName = tabName;
            this.table = table;
        }

        public Optional<String> getTabName() {
            return Optional.ofNullable(tabName);
        }

        public Table getTable() {
            return table;
        }
    }
}
