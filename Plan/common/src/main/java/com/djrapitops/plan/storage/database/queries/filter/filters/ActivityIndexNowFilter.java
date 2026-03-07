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
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Map;

/**
 * Filter for current activity index group.
 */
@Singleton
public class ActivityIndexNowFilter extends ActivityIndexOnDateFilter {

    @Inject
    public ActivityIndexNowFilter(PlanConfig config, DBSystem dbSystem) {
        super(config, dbSystem);
    }

    @Override
    public String getKind() {
        return "activityIndexNow";
    }

    @Override
    public Map<String, Object> getOptions() {
        return Collections.singletonMap("options", getOptionsArray());
    }

    @Override
    protected long getDate(InputFilterDto query) {
        return System.currentTimeMillis();
    }
}
