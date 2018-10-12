package com.djrapitops.plan.utilities.html.graphs;

import com.djrapitops.plan.data.store.mutators.formatting.Formatters;

/**
 * Utility for creating ProgressBars.
 *
 * @author Rsl1122
 */
public class ProgressBar {

    private final int obtained;
    private final int max;

    private final String color;

    public ProgressBar(int obtained, int max) {
        this(obtained, max, "teal");
    }

    public ProgressBar(int obtained, int max, String color) {
        this.obtained = obtained;
        this.max = max;
        this.color = color;
    }

    public String toHtml() {
        double percentage = obtained * 1.0 / max;
        int percentageRounded = (int) percentage;

        return "<div class=\"progress\"><div class=\"progress-bar bg-" + color + "\" role=\"progressbar\"" +
                " aria-valuenow=\"" + obtained + "\"" +
                " aria-valuemin=\"0\" aria-valuemax=\"" + max + "\"" +
                " style=\"width: " + percentageRounded + "%;\">" +
                obtained + " / " + max + " (" + Formatters.percentage().apply(percentage) + ")" +
                "</div></div>";
    }

}