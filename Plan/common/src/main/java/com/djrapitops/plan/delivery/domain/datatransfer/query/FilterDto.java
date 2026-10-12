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
package com.djrapitops.plan.delivery.domain.datatransfer.query;

import com.djrapitops.plan.modules.FiltersModule;
import com.djrapitops.plan.storage.database.queries.filter.Filter;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a query filter.
 *
 * @see Filter
 * @see FiltersModule
 */
public record FilterDto(String kind, Map<String, Object> options,
                        String[] expectedParameters) implements Comparable<FilterDto> {

    public FilterDto(String kind, Filter filter) {
        this(kind, filter.getOptions(), filter.getExpectedParameters());
    }

    @Override
    public int compareTo(FilterDto o) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.kind, o.kind);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FilterDto filterDto = (FilterDto) o;
        return Objects.equals(kind(), filterDto.kind()) && Objects.equals(options(), filterDto.options()) && Objects.deepEquals(expectedParameters(), filterDto.expectedParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind(), options(), Arrays.hashCode(expectedParameters()));
    }

    @Override
    public @NonNull String toString() {
        return "FilterDto{" +
                "kind='" + kind + '\'' +
                ", options=" + options +
                ", expectedParameters=" + Arrays.toString(expectedParameters) +
                '}';
    }
}
