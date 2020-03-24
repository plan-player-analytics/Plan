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
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.UUID;

@Singleton
public class PlayedBetweenDateRangeFilter extends DateRangeFilter {

    private DBSystem dbSystem;

    @Inject
    public PlayedBetweenDateRangeFilter(DBSystem dbSystem) {
        super(dbSystem);
        this.dbSystem = dbSystem;
    }

    @Override
    public String getKind() {
        return "playedBetween";
    }

    @Override
    public Set<UUID> getMatchingUUIDs(FilterQuery query) {
        long after = getAfter(query);
        long before = getBefore(query);
        return dbSystem.getDatabase().query(SessionQueries.uuidsOfPlayedBetween(after, before));
    }
}
