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
package com.djrapitops.plan.delivery.domain.datatransfer.extension;

import com.djrapitops.plan.extension.table.TableColumnFormat;

import java.util.Objects;

/**
 * @author AuroraLS3
 */
public class TableCellDto {

    private final Object value;
    private final TableColumnFormat format;

    public TableCellDto(Object value, TableColumnFormat format) {
        this.value = value;
        this.format = format;
    }

    public Object getValue() {
        return value;
    }

    public TableColumnFormat getFormat() {
        return format;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TableCellDto that = (TableCellDto) o;
        return Objects.equals(getValue(), that.getValue()) && getFormat() == that.getFormat();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getFormat());
    }

    @Override
    public String toString() {
        return "TableCellDto{" +
                "value=" + value +
                ", format=" + format +
                '}';
    }
}
