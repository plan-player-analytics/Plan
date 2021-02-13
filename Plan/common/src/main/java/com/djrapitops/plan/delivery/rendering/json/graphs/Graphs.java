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
package com.djrapitops.plan.delivery.rendering.json.graphs;

import com.djrapitops.plan.delivery.rendering.json.graphs.bar.BarGraphFactory;
import com.djrapitops.plan.delivery.rendering.json.graphs.calendar.CalendarFactory;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.LineGraphFactory;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.PieGraphFactory;
import com.djrapitops.plan.delivery.rendering.json.graphs.special.SpecialGraphFactory;
import com.djrapitops.plan.delivery.rendering.json.graphs.stack.StackGraphFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory class for different objects representing HTML graphs.
 *
 * @author AuroraLS3
 */
@Singleton
public class Graphs {

    private final BarGraphFactory barGraphFactory;
    private final CalendarFactory calendarFactory;
    private final LineGraphFactory lineGraphFactory;
    private final PieGraphFactory pieGraphFactory;
    private final StackGraphFactory stackGraphFactory;
    private final SpecialGraphFactory specialGraphFactory;

    @Inject
    public Graphs(
            BarGraphFactory barGraphFactory,
            CalendarFactory calendarFactory,
            LineGraphFactory lineGraphFactory,
            PieGraphFactory pieGraphFactory,
            StackGraphFactory stackGraphFactory,
            SpecialGraphFactory specialGraphFactory
    ) {
        this.barGraphFactory = barGraphFactory;
        this.calendarFactory = calendarFactory;
        this.lineGraphFactory = lineGraphFactory;
        this.pieGraphFactory = pieGraphFactory;
        this.stackGraphFactory = stackGraphFactory;
        this.specialGraphFactory = specialGraphFactory;
    }

    public BarGraphFactory bar() {
        return barGraphFactory;
    }

    public CalendarFactory calendar() {
        return calendarFactory;
    }

    public LineGraphFactory line() {
        return lineGraphFactory;
    }

    public PieGraphFactory pie() {
        return pieGraphFactory;
    }

    public StackGraphFactory stack() {
        return stackGraphFactory;
    }

    public SpecialGraphFactory special() {
        return specialGraphFactory;
    }
}