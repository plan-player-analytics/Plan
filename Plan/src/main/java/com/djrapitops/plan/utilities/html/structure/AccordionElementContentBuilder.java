/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.structure;

import java.io.Serializable;

/**
 * Utility for filling AccordionElements.
 *
 * @author Rsl1122
 * @see AccordionElement
 */
public class AccordionElementContentBuilder {

    private StringBuilder html;

    public AccordionElementContentBuilder() {
        this.html = new StringBuilder();
    }

    public AccordionElementContentBuilder addRowBold(String color, String icon, String text, Serializable value) {
        html.append("<p><i class=\"col-")
                .append(color).append(" fa fa-").append(icon)
                .append("\"></i> ").append(text);
        html.append("<span class=\"pull-right\"><b>").append(value).append("</b></span></p>");
        return this;
    }

    public AccordionElementContentBuilder addRow(String color, String icon, String text, Serializable value) {
        html.append("<p><i class=\"col-")
                .append(color).append(" fa fa-").append(icon)
                .append("\"></i> ").append(text);
        html.append("<span class=\"pull-right\">").append(value).append("</span></p>");
        return this;
    }

    public AccordionElementContentBuilder addHtml(String html) {
        this.html.append(html);
        return this;
    }

    public String toHtml() {
        return html.toString();
    }

    public AccordionElementContentBuilder addBreak() {
        html.append("<br>");
        return this;
    }
}