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
package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.domain.TimeSegment;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.utilities.comparators.DateHolderOldestComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeSegmentsMutator<T> {

    List<TimeSegment<T>> segments;

    public TimeSegmentsMutator(List<TimeSegment<T>> segments) {
        this.segments = segments;
    }

    public static TimeSegmentsMutator<Integer> sessionClockSegments(List<FinishedSession> sessions) {
        List<DateObj<Integer>> changes = new ArrayList<>();

        for (FinishedSession session : sessions) {
            long startTime = (session.getStart()) % TimeUnit.DAYS.toMillis(1);
            long endTime = (session.getEnd()) % TimeUnit.DAYS.toMillis(1);
            changes.add(new DateObj<>(startTime, 1));
            changes.add(new DateObj<>(endTime, -1));
        }

        changes.sort(new DateHolderOldestComparator());

        int count = 0;
        long previousTime = 0L;
        List<TimeSegment<Integer>> segments = new ArrayList<>();
        for (DateObj<Integer> change : changes) {
            segments.add(new TimeSegment<>(previousTime, change.getDate(), count));
            count += change.getValue();
            previousTime = change.getDate();
        }

        return new TimeSegmentsMutator<>(segments);
    }
}
