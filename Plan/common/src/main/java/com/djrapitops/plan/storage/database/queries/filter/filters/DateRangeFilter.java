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
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.filter.Filter;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.Maps;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;

public abstract class DateRangeFilter implements Filter {

    public static final String AFTER_DATE = "afterDate";
    public static final String AFTER_TIME = "afterTime";
    public static final String BEFORE_DATE = "beforeDate";
    public static final String BEFORE_TIME = "beforeTime";

    private static final String DATE_FORMAT = "dd/MM/yyyy kk:mm";

    private final DBSystem dbSystem;

    protected DateRangeFilter(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    public static long getTime(@Untrusted InputFilterDto query, String dateKey, String timeKey, String kind) {
        @Untrusted String date = query.get(dateKey).orElseThrow(() -> new BadRequestException("'" + dateKey + "' not specified in parameters for " + kind));
        @Untrusted String time = query.get(timeKey).orElseThrow(() -> new BadRequestException("'" + timeKey + "' not specified in parameters for " + kind));

        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(date + ' ' + time).getTime();
        } catch (@Untrusted ParseException e) {
            throw new IllegalArgumentException("Could not parse date from given '" + dateKey + "' and '" + timeKey + "' - expected format dd/MM/yyyy and kk:mm");
        }
    }

    public static Map<String, Object> getOptions(Database database) {
        Optional<Long> earliestData = database.query(BaseUserQueries.minimumRegisterDate());
        long now = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        String[] afterDate = StringUtils.split(formatter.format(earliestData.orElse(now)), ' ');
        String[] beforeDate = StringUtils.split(formatter.format(now), ' ');
        return Maps.builder(String.class, Object.class)
                .put("after", afterDate)
                .put("before", beforeDate)
                .build();
    }

    @Override
    public String[] getExpectedParameters() {
        return new String[]{
                AFTER_DATE,
                AFTER_TIME,
                BEFORE_DATE,
                BEFORE_TIME
        };
    }

    @Override
    public Map<String, Object> getOptions() {
        Database database = dbSystem.getDatabase();
        return getOptions(database);
    }

    protected long getAfter(@Untrusted InputFilterDto query) {
        if (query.get(AFTER_DATE).isEmpty() || query.get(AFTER_TIME).isEmpty()) {
            return 0L;
        }
        return getTime(query, AFTER_DATE, AFTER_TIME, getKind());
    }

    protected long getBefore(@Untrusted InputFilterDto query) {
        if (query.get(BEFORE_DATE).isEmpty() || query.get(BEFORE_TIME).isEmpty()) {
            return Long.MAX_VALUE;
        }
        return getTime(query, BEFORE_DATE, BEFORE_TIME, getKind());
    }
}
