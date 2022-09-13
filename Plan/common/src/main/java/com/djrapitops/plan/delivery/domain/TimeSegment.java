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
package com.djrapitops.plan.delivery.domain;

import java.util.Comparator;
import java.util.Objects;

public class TimeSegment<T> {

    private final long start;
    private final long end;
    private final T value;

    public TimeSegment(long start, long end, T value) {
        this.start = start;
        this.end = end;
        this.value = value;
    }

    public static <V> Comparator<TimeSegment<V>> earliestStartFirstComparator() {
        return Comparator.comparingLong(segment -> segment.start);
    }

    public static <V> Comparator<TimeSegment<V>> earliestEndFirstComparator() {
        return Comparator.comparingLong(segment -> segment.end);
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSegment<?> that = (TimeSegment<?>) o;
        return getStart() == that.getStart() && getEnd() == that.getEnd() && Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), getValue());
    }

    @Override
    public String toString() {
        return "TimeSegment{" +
                "start=" + start +
                ", end=" + end +
                ", value=" + value +
                '}';
    }
}
