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
package com.djrapitops.plan.delivery.domain.datatransfer;

import com.djrapitops.plan.storage.database.queries.filter.Filter;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a query filter.
 *
 * @see Filter
 * @see com.djrapitops.plan.modules.FiltersModule
 */
public class FilterDto implements Comparable<FilterDto> {
    private final String kind;
    private final Map<String, Object> options;
    private final String[] expectedParameters;

    public FilterDto(String kind, Filter filter) {
        this.kind = kind;
        this.options = filter.getOptions();
        this.expectedParameters = filter.getExpectedParameters();
    }

    public String getKind() {
        return kind;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public String[] getExpectedParameters() {
        return expectedParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterDto that = (FilterDto) o;
        return Objects.equals(kind, that.kind) && Objects.equals(options, that.options) && Arrays.equals(expectedParameters, that.expectedParameters);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(kind, options);
        result = 31 * result + Arrays.hashCode(expectedParameters);
        return result;
    }

    @Override
    public int compareTo(FilterDto o) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.kind, o.kind);
    }
}
