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

import java.util.Objects;

/**
 * Object that has a value tied to a date.
 *
 * @author AuroraLS3
 */
public class DateObj<T> implements DateHolder {

    private final long date;
    private T value;

    public DateObj(long date, T value) {
        this.date = date;
        this.value = value;
    }

    @Override
    public long getDate() {
        return date;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateObj<?> dateObj = (DateObj<?>) o;
        return getDate() == dateObj.getDate() && Objects.equals(getValue(), dateObj.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getValue());
    }

    @Override
    public String toString() {
        return "DateObj{" +
                "date=" + date +
                ", value=" + value +
                '}';
    }
}