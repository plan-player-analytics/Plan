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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents parameters for a single {@link Filter} parsed from the query json.
 *
 * @author Rsl1122
 */
public class FilterQuery {

    private final String kind;
    private final Map<String, String> parameters;

    public FilterQuery(String kind, Map<String, String> parameters) {
        this.kind = kind;
        this.parameters = parameters;
    }

    public static List<FilterQuery> parse(String json) throws IOException {
        return new Gson().getAdapter(new TypeToken<List<FilterQuery>>() {}).fromJson(json);
    }

    public String getKind() {
        return kind;
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(parameters.get(key));
    }
}
