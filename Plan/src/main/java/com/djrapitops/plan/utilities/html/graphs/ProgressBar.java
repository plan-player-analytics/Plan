package com.djrapitops.plan.utilities.html.graphs;

import com.djrapitops.plan.utilities.formatting.Formatter;

/**
 * Utility for creating Progress bars.
 *
 * @author Rsl1122
 */
public class ProgressBar {

    private final int obtained;
    private final int max;

    private final Formatter<Double> percentageFormatter;

    private final String color;

    public ProgressBar(int obtained, int max, Formatter<Double> percentageFormatter) {
        this(obtained, max, "teal", percentageFormatter);
    }

    public ProgressBar(int obtained, int max, String color, Formatter<Double> percentageFormatter) {
        this.obtained = obtained;
        this.max = max;
        this.color = color;
        this.percentageFormatter = percentageFormatter;
    }

    public String toHtml() {
        double percentage = obtained * 1.0 / max;
        int percentageRounded = (int) percentage;

        return "<div class=\"progress\"><div class=\"progress-bar bg-" + color + "\" role=\"progressbar\"" +
                " aria-valuenow=\"" + obtained + "\"" +
                " aria-valuemin=\"0\" aria-valuemax=\"" + max + "\"" +
                " style=\"width: " + percentageRounded + "%;\">" +
                obtained + " / " + max + " (" + percentageFormatter.apply(percentage) + ")" +
                "</div></div>";
    }

}