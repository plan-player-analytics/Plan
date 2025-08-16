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
import com.djrapitops.plan.settings.locale.lang.FilterLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.filter.CompleteSetException;
import com.djrapitops.plan.storage.database.queries.objects.UserInfoQueries;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class OperatorsFilter extends MultiOptionFilter {

    private final DBSystem dbSystem;

    @Inject
    public OperatorsFilter(
            DBSystem dbSystem
    ) {
        this.dbSystem = dbSystem;
    }

    @Override
    public String getKind() {
        return "operators";
    }

    private String[] getOptionsArray() {
        return new String[]{FilterLang.OPERATORS.getKey(), FilterLang.NON_OPERATORS.getKey()};
    }

    @Override
    public Map<String, Object> getOptions() {
        return Collections.singletonMap("options", getOptionsArray());
    }

    @Override
    public Set<Integer> getMatchingUserIds(@Untrusted InputFilterDto query) {
        @Untrusted List<String> selected = getSelected(query);
        Set<Integer> userIds = new HashSet<>();
        String[] options = getOptionsArray();

        boolean includeOperators = selected.contains(options[0]);
        boolean includeNonOperators = selected.contains(options[1]);

        if (includeOperators && includeNonOperators) throw new CompleteSetException(); // Full set, no need for query
        if (includeOperators) userIds.addAll(dbSystem.getDatabase().query(UserInfoQueries.userIdsOfOperators()));
        if (includeNonOperators) userIds.addAll(dbSystem.getDatabase().query(UserInfoQueries.userIdsOfNonOperators()));
        return userIds;
    }
}
