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