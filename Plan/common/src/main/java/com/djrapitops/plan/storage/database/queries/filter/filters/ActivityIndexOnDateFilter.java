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
import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.filter.CompleteSetException;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filter for activity index group at a specific time.
 */
@Singleton
public class ActivityIndexOnDateFilter implements MultiOptionFilter {

    protected final PlanConfig config;
    protected final DBSystem dbSystem;

    @Inject
    public ActivityIndexOnDateFilter(PlanConfig config, DBSystem dbSystem) {
        this.dbSystem = dbSystem;
        this.config = config;
    }

    protected long getDate(@Untrusted InputFilterDto query) {
        return DateRangeFilter.getTime(query, "date", "time", getKind());
    }

    @Override
    public String[] getExpectedParameters() {
        return new String[]{
                SELECTED,
                "date",
                "time"
        };
    }

    @Override
    public String getKind() {
        return "activityIndexOn";
    }

    protected String[] getOptionsArray() {
        return ActivityIndex.getGroupLocaleKeys();
    }

    @Override
    public Map<String, Object> getOptions() {
        Map<String, Object> options = DateRangeFilter.getOptions(dbSystem.getDatabase());
        options.put("options", getOptionsArray());
        return options;
    }

    @Override
    public Set<Integer> getMatchingUserIds(@Untrusted InputFilterDto query) {
        @Untrusted List<String> selected = getSelected(query);
        String[] options = getOptionsArray();

        boolean includeVeryActive = selected.contains(options[0]);
        boolean includeActive = selected.contains(options[1]);
        boolean includeRegular = selected.contains(options[2]);
        boolean includeIrregular = selected.contains(options[3]);
        boolean includeInactive = selected.contains(options[4]);

        if (includeVeryActive && includeActive && includeRegular && includeIrregular && includeInactive) {
            throw new CompleteSetException(); // Full set, no need for query
        }
        long date = getDate(query);
        long playtimeThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);
        Map<Integer, ActivityIndex> indexes = dbSystem.getDatabase().query(NetworkActivityIndexQueries.activityIndexForAllPlayers(date, playtimeThreshold));

        return indexes.entrySet().stream()
                .filter(entry -> selected.contains(entry.getValue().getGroupLocaleKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
