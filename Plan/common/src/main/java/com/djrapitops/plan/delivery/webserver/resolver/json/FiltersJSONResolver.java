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

import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.storage.database.queries.filter.Filter;
import com.djrapitops.plan.storage.database.queries.filter.QueryFilters;
import com.djrapitops.plan.utilities.java.Maps;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class FiltersJSONResolver implements Resolver {

    private QueryFilters filters;

    @Inject
    public FiltersJSONResolver(
            QueryFilters filters
    ) {
        this.filters = filters;
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
                .setJSONContent(Maps.builder(String.class, Object.class)
                        .put("filters", serializeFilters())
                        .build())
                .build();
    }

    private List<Map<String, Object>> serializeFilters() {
        List<Map<String, Object>> filterList = new ArrayList<>();
        for (Map.Entry<String, Filter> entry : filters.getFilters().entrySet()) {
            filterList.add(Maps.builder(String.class, Object.class)
                    .put("kind", entry.getKey())
                    .put("options", entry.getValue().getOptions())
                    .put("expectedParameters", entry.getValue().getExpectedParameters())
                    .build());
        }
        return filterList;
    }
}
