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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a query filter for /query page.
 *
 * @author Rsl1122
 */
public interface Filter {

    String getKind();

    String[] getExpectedParameters();

    default List<String> getOptions() {
        return Collections.emptyList();
    }

    /**
     * Match some UUIDs to the filter.
     *
     * @param query Query for the filter
     * @return Set of UUIDs this filter applies to
     * @throws IllegalArgumentException If the arguments are not valid.
     * @throws CompleteSetException     If the arguments produce a complete set.
     */
    Set<UUID> getMatchingUUIDs(FilterQuery query);

}
