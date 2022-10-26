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

import com.djrapitops.plan.extension.annotation.TableProvider;
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.table.Table;

import java.util.function.Supplier;

public class TableDataProvider extends AnnotationDataProvider<TableProvider, Table, Table> {

    public TableDataProvider(TableProvider provider, ExtensionMethod method) {
        super(provider, method, Table.class, Table.class);
    }

    @Override
    public DataValue<Table> addDataValueToBuilder(ValueBuilder builder, Supplier<Table> dataSupplier) {
        return builder.buildTable(dataSupplier, provider.tableColor());
    }
}
