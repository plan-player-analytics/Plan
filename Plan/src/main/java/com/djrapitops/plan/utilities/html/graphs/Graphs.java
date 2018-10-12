package com.djrapitops.plan.utilities.html.graphs;

import com.djrapitops.plan.utilities.html.graphs.bar.BarGraphFactory;
import com.djrapitops.plan.utilities.html.graphs.calendar.CalendarFactory;
import com.djrapitops.plan.utilities.html.graphs.line.LineGraphFactory;
import com.djrapitops.plan.utilities.html.graphs.pie.PieGraphFactory;
import com.djrapitops.plan.utilities.html.graphs.special.SpecialGraphFactory;
import com.djrapitops.plan.utilities.html.graphs.stack.StackGraphFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory class for different objects representing HTML graphs.
 *
 * @author Rsl1122
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