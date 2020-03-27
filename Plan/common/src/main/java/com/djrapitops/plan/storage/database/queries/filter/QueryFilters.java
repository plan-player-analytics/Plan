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

import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

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

    /**
     * Apply queries to get a {@link com.djrapitops.plan.storage.database.queries.filter.Filter.Result}.
     *
     * @param filterQueries FilterQueries to use as filter parameters.
     * @return the result object or null if none of the filterQueries could be applied.
     * @throws BadRequestException If the request kind is not supported or if filter was given bad options.
     */
    public Filter.Result apply(List<FilterQuery> filterQueries) {
        Filter.Result current = null;
        for (FilterQuery filterQuery : filterQueries) {
            current = apply(current, filterQuery);
            if (current != null && current.isEmpty()) break;
        }
        return current;
    }

    private Filter.Result apply(Filter.Result current, FilterQuery filterQuery) {
        String kind = filterQuery.getKind();
        Filter filter = getFilter(kind).orElseThrow(() -> new BadRequestException("Filter kind not supported: '" + kind + "'"));

        current = getResult(current, filter, filterQuery);
        return current;
    }

    private Filter.Result getResult(Filter.Result current, Filter filter, FilterQuery query) {
        try {
            return current == null ? filter.apply(query) : current.apply(filter, query);
        } catch (IllegalArgumentException badOptions) {
            throw new BadRequestException("Bad parameters for filter '" + filter.getKind() +
                    "': expecting " + Arrays.asList(filter.getExpectedParameters()) +
                    ", but was given " + query.getSetParameters());
        } catch (CompleteSetException complete) {
            return current == null ? null : current.notApplied(filter);
        }
    }

    public Map<String, Filter> getFilters() {
        return filters;
    }
}
