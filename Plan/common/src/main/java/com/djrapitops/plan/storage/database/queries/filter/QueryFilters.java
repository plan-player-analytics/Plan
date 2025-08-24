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

import com.djrapitops.plan.delivery.domain.datatransfer.InputFilterDto;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.filter.filters.AllPlayersFilter;
import com.djrapitops.plan.storage.database.queries.filter.filters.PluginGroupsFilter;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Contains a single instance of each filter kind.
 *
 * @author AuroraLS3
 * @see com.djrapitops.plan.modules.FiltersModule for list of Filters
 */
@Singleton
public class QueryFilters {

    private final Map<String, Filter> filters;
    private final AllPlayersFilter allPlayersFilter;
    private final DBSystem dbSystem;
    private final PluginGroupsFilter.PluginGroupsFilterQuery filterQuery;

    private final AtomicBoolean fetchedPluginFilters = new AtomicBoolean(false);

    @Inject
    public QueryFilters(
            Set<Filter> filters,
            AllPlayersFilter allPlayersFilter,
            DBSystem dbSystem,
            PluginGroupsFilter.PluginGroupsFilterQuery filterQuery
    ) {
        this.allPlayersFilter = allPlayersFilter;
        this.dbSystem = dbSystem;
        this.filterQuery = filterQuery;
        this.filters = new HashMap<>();
        put(filters);
    }

    private void put(Iterable<? extends Filter> filters) {
        for (Filter filter : filters) {
            this.filters.put(filter.getKind(), filter);
        }
    }

    private void prepareFilters() {
        if (!fetchedPluginFilters.get()) {
            put(dbSystem.getDatabase().query(filterQuery));
            fetchedPluginFilters.set(true);
        }
    }

    public Optional<Filter> getFilter(@Untrusted String kind) {
        prepareFilters();
        return Optional.ofNullable(filters.get(kind));
    }

    /**
     * Apply queries to get a {@link com.djrapitops.plan.storage.database.queries.filter.Filter.Result}.
     *
     * @param filterQueries FilterQueries to use as filter parameters.
     * @return the result object or null if none of the filterQueries could be applied.
     * @throws BadRequestException If the request kind is not supported or if filter was given bad options.
     */
    public Filter.Result apply(@Untrusted List<InputFilterDto> filterQueries) {
        prepareFilters();
        Filter.Result current = null;
        if (filterQueries.isEmpty()) return allPlayersFilter.apply(null);
        for (@Untrusted InputFilterDto inputFilterDto : filterQueries) {
            current = apply(current, inputFilterDto);
            if (current != null && current.isEmpty()) break;
        }
        return current;
    }

    private Filter.Result apply(Filter.Result current, @Untrusted InputFilterDto inputFilterDto) {
        @Untrusted String kind = inputFilterDto.getKind();
        Filter filter = getFilter(kind).orElseThrow(() -> new BadRequestException("Given Filter 'kind' not supported"));

        return getResult(current, filter, inputFilterDto);
    }

    private Filter.Result getResult(Filter.Result current, Filter filter, @Untrusted InputFilterDto query) {
        try {
            return current == null ? filter.apply(query) : current.apply(filter, query);
        } catch (IllegalArgumentException badOptions) {
            throw new BadRequestException("Bad parameters for filter '" + filter.getKind() +
                    "': expecting " + Arrays.asList(filter.getExpectedParameters()) + " as parameters");
        }
    }

    public Map<String, Filter> getFilters() {
        prepareFilters();
        return filters;
    }
}
