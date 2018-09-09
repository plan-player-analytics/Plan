package com.djrapitops.plan.utilities.formatting;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class PercentageFormatter implements Formatter<Double> {

    private final Formatter<Double> formatter;

    public PercentageFormatter(Formatter<Double> formatter) {
        this.formatter = formatter;
    }

    @Override
    public String apply(Double value) {
        return value >= 0 ? formatter.apply(value * 100.0) + "%" : "-";
    }
}