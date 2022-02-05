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
package com.djrapitops.plan.extension.table;

import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Object for giving Plan table data.
 * <p>
 * Usage: {@code Table.builder().columnOne("columnName", new Icon(...)).addRow("Your", "Row", "Data").build()}
 * <p>
 * Tables about players can have up to 4 columns.
 * Tables about server can have up to 5 columns.
 * <p>
 * Icon colors are ignored.
 * <p>
 * If a row has more values than the column limit, they are ignored.
 * If a row has less values than table columns, a '-' is displayed to distinguish a missing value.
 * <p>
 * If a table has no columns or rows, it is ignored.
 *
 * @author AuroraLS3
 * @see com.djrapitops.plan.extension.annotation.TableProvider for associated annotation.
 */
public final class Table {

    private final String[] columns;
    private final Icon[] icons;
    private final TableColumnFormat[] tableColumnFormats;

    private final List<Object[]> rows;

    private Table() {
        /* Tables are constructed with the factory. */

        columns = new String[5];
        icons = new Icon[5];
        tableColumnFormats = new TableColumnFormat[]{
                TableColumnFormat.NONE, TableColumnFormat.NONE, TableColumnFormat.NONE,
                TableColumnFormat.NONE, TableColumnFormat.NONE
        };
        rows = new ArrayList<>();
    }

    /**
     * Create a new Table Factory.
     *
     * @return a new Table Factory.
     */
    public static Table.Factory builder() {
        return new Table.Factory();
    }

    public String[] getColumns() {
        return columns;
    }

    public int getMaxColumnSize() {
        int columnCount = 0;
        for (String column : columns) {
            if (column == null) {
                break; // Prevent having one null column between two columns
            }
            columnCount++;
        }
        return columnCount;
    }

    public Icon[] getIcons() {
        return icons;
    }

    public List<Object[]> getRows() {
        return rows;
    }

    public TableColumnFormat[] getTableColumnFormats() {
        return tableColumnFormats;
    }

    /**
     * Factory for creating new {@link Table} objects.
     */
    public static final class Factory {

        private final Table building;

        // These variable are related to implementation, and is used when the table is being fetched from the database.
        Color color;              // Table color is defined with TableProvider annotation.
        String tableName;         // tableName is defined by method name annotated by TableProvider.
        String tabName;           // Tab name is defined with Tab annotation
        int tabPriority;          // Tab priority is defined with TabOrder annotation
        ElementOrder[] tabOrder;  // Tab element order is defined with TabInfo annotation
        Icon tabIcon;             // Tab icon is defined with TabInfo annotation

        private Factory() {
            building = new Table();
        }

        private Factory column(int index, String columnName, Icon icon) {
            building.columns[index] = StringUtils.truncate(columnName, 50);
            building.icons[index] = icon != null ? Icon.called(icon.getName()).of(icon.getFamily()).build() : Icon.called("question").build();
            return this;
        }

        /**
         * Set first column name and icon.
         *
         * @param columnName Name of the column.
         * @param icon       Icon of the column, color is ignored.
         * @return Factory.
         */
        public Factory columnOne(String columnName, Icon icon) {
            return column(0, columnName, icon);
        }

        /**
         * Set second column name and icon.
         *
         * @param columnName Name of the column.
         * @param icon       Icon of the column, color is ignored.
         * @return Factory.
         */
        public Factory columnTwo(String columnName, Icon icon) {
            return column(1, columnName, icon);
        }

        /**
         * Set third column name and icon.
         *
         * @param columnName Name of the column.
         * @param icon       Icon of the column, color is ignored.
         * @return Factory.
         */
        public Factory columnThree(String columnName, Icon icon) {
            return column(2, columnName, icon);
        }

        /**
         * Set fourth column name and icon.
         *
         * @param columnName Name of the column.
         * @param icon       Icon of the column, color is ignored.
         * @return Factory.
         */
        public Factory columnFour(String columnName, Icon icon) {
            return column(3, columnName, icon);
        }

        /**
         * Set fifth column name and icon.
         *
         * @param columnName Name of the column.
         * @param icon       Icon of the column, color is ignored.
         * @return Factory.
         */
        public Factory columnFive(String columnName, Icon icon) {
            return column(4, columnName, icon);
        }

        private Factory columnFormat(int index, TableColumnFormat tableColumnFormat) {
            building.tableColumnFormats[index] = tableColumnFormat;
            return this;
        }

        public Factory columnOneFormat(TableColumnFormat tableColumnFormat) {
            return columnFormat(0, tableColumnFormat);
        }

        public Factory columnTwoFormat(TableColumnFormat tableColumnFormat) {
            return columnFormat(1, tableColumnFormat);
        }

        public Factory columnThreeFormat(TableColumnFormat tableColumnFormat) {
            return columnFormat(2, tableColumnFormat);
        }

        public Factory columnFourFormat(TableColumnFormat tableColumnFormat) {
            return columnFormat(3, tableColumnFormat);
        }

        public Factory columnFiveFormat(TableColumnFormat tableColumnFormat) {
            return columnFormat(4, tableColumnFormat);
        }

        /**
         * Add a row of values to the table.
         *
         * @param values One value per column you have defined, {@code Object#toString()} will be called on the objects.
         * @return Factory.
         * @throws IllegalArgumentException If given varargs for 'values' is null.
         */
        public Factory addRow(Object... values) {
            if (values == null) {
                throw new IllegalArgumentException("'values' for Table#addRow can not be null!");
            }

            if (values.length == 0 || areAllValuesNull(values)) {
                return this; // Ignore row when all values are null or no values are present.
            }

            Object[] row = new Object[5];

            for (int i = 0; i < Math.min(values.length, 5); i++) {
                Object value = values[i];
                if (value instanceof Optional) {
                    value = ((Optional<?>) value).map(Objects::toString).orElse("-");
                }
                row[i] = value;
            }

            building.rows.add(row);
            return this;
        }

        private boolean areAllValuesNull(Object[] values) {
            boolean allNull = true;
            for (Object value : values) {
                if (value != null) {
                    allNull = false;
                    break;
                }
            }
            return allNull;
        }

        /**
         * Finish building the table.
         *
         * @return Finished Table object.
         */
        public Table build() {
            return building;
        }
    }

}