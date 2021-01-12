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
package com.djrapitops.plan.delivery.webserver.resolver.json;

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.storage.database.queries.filter.Filter;
import com.djrapitops.plan.storage.database.queries.filter.QueryFilters;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class FiltersJSONResolver implements Resolver {

    private final QueryFilters filters;
    private final Formatters formatters;

    @Inject
    public FiltersJSONResolver(
            QueryFilters filters,
            Formatters formatters
    ) {
        this.filters = filters;
        this.formatters = formatters;
    }

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        return user.hasPermission("page.players");
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse());
    }

    private Response getResponse() {
        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(new FilterResponseJSON(
                        filters.getFilters(),
                        new ViewJSON(formatters)
                )).build();
    }

    /**
     * JSON serialization class.
     */
    static class FilterResponseJSON {
        final List<FilterJSON> filters;
        final ViewJSON view;

        public FilterResponseJSON(Map<String, Filter> filtersByKind, ViewJSON view) {
            this.filters = new ArrayList<>();
            for (Map.Entry<String, Filter> entry : filtersByKind.entrySet()) {
                filters.add(new FilterJSON(entry.getKey(), entry.getValue()));
            }
            this.view = view;
        }
    }

    /**
     * JSON serialization class.
     */
    static class FilterJSON {
        final String kind;
        final Map<String, Object> options;
        final String[] expectedParameters;

        public FilterJSON(String kind, Filter filter) {
            this.kind = kind;
            this.options = filter.getOptions();
            this.expectedParameters = filter.getExpectedParameters();
        }
    }

    /**
     * JSON serialization class.
     */
    static class ViewJSON {
        final String afterDate;
        final String afterTime;
        final String beforeDate;
        final String beforeTime;

        public ViewJSON(Formatters formatters) {
            long now = System.currentTimeMillis();
            long monthAgo = now - TimeUnit.DAYS.toMillis(30);

            Formatter<Long> formatter = formatters.javascriptDateFormatterLong();
            String[] after = StringUtils.split(formatter.apply(monthAgo), " ");
            String[] before = StringUtils.split(formatter.apply(now), " ");

            this.afterDate = after[0];
            this.afterTime = after[1];
            this.beforeDate = before[0];
            this.beforeTime = before[1];
        }
    }
}
