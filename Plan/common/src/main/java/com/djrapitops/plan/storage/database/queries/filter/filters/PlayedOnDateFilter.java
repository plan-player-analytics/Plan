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

import com.djrapitops.plan.delivery.domain.datatransfer.InputFilterDto;
import com.djrapitops.plan.storage.database.DBSystem;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Filter for getting who played on specific time on.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlayedOnDateFilter extends PlayedBetweenDateRangeFilter {

    @Inject
    public PlayedOnDateFilter(DBSystem dbSystem) {
        super(dbSystem);
    }

    @Override
    public String getKind() {
        return "playedOn";
    }

    @Override
    public String[] getExpectedParameters() {
        return new String[]{
                DateRangeFilter.AFTER_DATE,
                DateRangeFilter.AFTER_TIME
        };
    }

    @Override
    protected long getBefore(InputFilterDto query) {
        return super.getAfter(query);
    }
}
