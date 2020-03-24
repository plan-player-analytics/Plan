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
package com.djrapitops.plan.storage.database.queries.filter.filters;

import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.filter.FilterQuery;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PluginGroupsFilter extends MultiOptionFilter {

    private final DBSystem dbSystem;
    private final String pluginName;
    private final List<String> groups;

    public PluginGroupsFilter(
            DBSystem dbSystem,
            String pluginName,
            List<String> groups
    ) {
        this.dbSystem = dbSystem;
        this.pluginName = pluginName;
        this.groups = groups;
    }

    @Override
    public String getKind() {
        return null;
    }

    @Override
    public List<String> getOptions() {
        return null;
    }

    @Override
    public Set<UUID> getMatchingUUIDs(FilterQuery query) {
        return null;
    }
}
