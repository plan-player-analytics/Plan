package com.djrapitops.plan.system.json;

import com.djrapitops.plan.utilities.formatting.Formatter;

/**
 * Represents a trend in the data - used for JSON format.
 *
 * @author Rsl1122
 */
public class Trend {

    private String text;
    private String direction;
    private boolean reversed;

    public Trend(long before, long after, boolean reversed) {
        long difference = Math.abs(before - after);
        this.text = Long.toString(difference);
        this.direction = getDirection(before, after);
        this.reversed = reversed;
    }

    public Trend(long before, long after, boolean reversed, Formatter<Long> formatter) {
        long difference = Math.abs(before - after);
        this.text = formatter.apply(difference);
        this.direction = getDirection(before, after);
        this.reversed = reversed;
    }

    public Trend(double before, double after, boolean reversed, Formatter<Double> formatter) {
        double difference = Math.abs(before - after);
        this.text = formatter.apply(difference);
        this.direction = getDirection(before, after);
        this.reversed = reversed;
    }

    private String getDirection(double before, double after) {
        if (before < after) {
            return "+";
        } else if (before > after) {
            return "-";
        }
        return null;
    }

}