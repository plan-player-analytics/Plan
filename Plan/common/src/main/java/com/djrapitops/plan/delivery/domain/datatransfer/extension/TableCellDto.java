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

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author AuroraLS3
 */
public class TableCellDto {

    private final String value;
    @Nullable
    private final Object valueUnformatted;

    public TableCellDto(String value) {
        this.value = value;
        this.valueUnformatted = null;
    }

    public TableCellDto(String value, @Nullable Object valueUnformatted) {
        this.value = value;
        this.valueUnformatted = valueUnformatted;
    }

    public String getValue() {
        return value;
    }

    @Nullable
    public Object getValueUnformatted() {
        return valueUnformatted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableCellDto that = (TableCellDto) o;
        return Objects.equals(getValue(), that.getValue()) && Objects.equals(getValueUnformatted(), that.getValueUnformatted());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getValueUnformatted());
    }

    @Override
    public String toString() {
        return "TableCellDto{" +
                "value='" + value + '\'' +
                ", valueUnformatted=" + valueUnformatted +
                '}';
    }
}
