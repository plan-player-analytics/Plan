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
package com.djrapitops.plan.delivery.rendering.json.graphs.calendar;

import java.io.Serializable;
import java.util.Optional;

/**
 * Represents an entry for FullCalendar json calendar.
 *
 * @author AuroraLS3
 */
public class CalendarEntry {

    private final String title;
    private final Serializable value;
    private final Serializable start;
    private Serializable end;
    private String color;

    private CalendarEntry(String title, Serializable value, Serializable start) {
        this.title = title;
        this.value = value;
        this.start = start;
    }

    public static CalendarEntry of(String title, Serializable value, Serializable start) {
        return new CalendarEntry(title, value, start);
    }

    public CalendarEntry withEnd(Serializable end) {
        this.end = end;
        return this;
    }

    public CalendarEntry withColor(String color) {
        this.color = color;
        return this;
    }

    public Serializable getTitle() {
        return title;
    }

    public Serializable getValue() {
        return value;
    }

    public Serializable getStart() {
        return start;
    }

    public Optional<Serializable> getEnd() {
        return Optional.ofNullable(end);
    }

    public Optional<String> getColor() {
        return Optional.ofNullable(color);
    }

    @Override
    public String toString() {
        return "{" +
                "title:'" + title + '\'' +
                ", value:" + value +
                ", start:'" + start + '\'' +
                (end != null ? ", end='" + end + '\'' : "") +
                (color != null ? ", color='" + color + '\'' : "") +
                '}';
    }
}
