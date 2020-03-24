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
package com.djrapitops.plan.storage.database.queries.filter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Contains a single instance of each filter kind.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.modules.FiltersModule for list of Filters
 */
@Singleton
public class QueryFilters {

    private Map<String, Filter> filters;

    @Inject
    public QueryFilters(Set<Filter> filters) {
        this.filters = new HashMap<>();
        put(filters);
    }

    private void put(Iterable<Filter> filters) {
        for (Filter filter : filters) {
            this.filters.put(filter.getKind(), filter);
        }
    }

    public Optional<Filter> getFilter(String kind) {
        return Optional.ofNullable(filters.get(kind));
    }

}
