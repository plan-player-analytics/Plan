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

import com.djrapitops.plan.delivery.domain.datatransfer.InputFilterDto;
import com.djrapitops.plan.utilities.dev.Untrusted;

import java.util.*;

/**
 * Represents a query filter for /query page.
 *
 * @author AuroraLS3
 */
public interface Filter {

    String getKind();

    String[] getExpectedParameters();

    default Map<String, Object> getOptions() {
        return Collections.emptyMap();
    }

    /**
     * Match some UUIDs to the filter.
     *
     * @param query Query for the filter
     * @return Set of UUIDs this filter applies to
     * @throws IllegalArgumentException If the arguments are not valid.
     */
    Set<Integer> getMatchingUserIds(@Untrusted InputFilterDto query);

    default Result apply(@Untrusted InputFilterDto query) {
        try {
            return new Result(null, getKind(), getMatchingUserIds(query));
        } catch (CompleteSetException allMatch) {
            return new Result(null, getKind() + " (skip)", new HashSet<>());
        }
    }

    class Result {
        private final Result previous;

        private final String filterKind;
        private final int resultSize;
        private final Set<Integer> currentUserIds;

        private Result(Result previous, String filterKind, Set<Integer> currentUserIds) {
            this.previous = previous;
            this.filterKind = filterKind;
            this.resultSize = currentUserIds.size();
            this.currentUserIds = currentUserIds;
        }

        public Result apply(Filter filter, InputFilterDto query) {
            try {
                Set<Integer> got = filter.getMatchingUserIds(query);
                currentUserIds.retainAll(got);
                return new Result(this, filter.getKind(), currentUserIds);
            } catch (CompleteSetException allMatch) {
                return notApplied(filter);
            }
        }

        public Result notApplied(Filter filter) {
            return new Result(this, filter.getKind() + " (skip)", currentUserIds);
        }

        public boolean isEmpty() {
            return resultSize <= 0;
        }

        public Set<Integer> getResultUserIds() {
            return currentUserIds;
        }

        public List<ResultPath> getInverseResultPath() {
            List<ResultPath> path = new ArrayList<>();

            Result current = this;
            while (current != null) {
                path.add(new ResultPath(current.filterKind, current.resultSize));
                current = current.previous;
            }

            return path;
        }
    }

    class ResultPath {
        final String kind;
        final int size;

        public ResultPath(String kind, int size) {
            this.kind = kind;
            this.size = size;
        }
    }
}
