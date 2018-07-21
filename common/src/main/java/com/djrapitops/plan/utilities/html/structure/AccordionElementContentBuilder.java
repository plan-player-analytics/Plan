/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.structure;


import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;

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

    @Deprecated
    public AccordionElementContentBuilder addRowBold(String color, String icon, String text, Serializable value) {
        return addRowBold(Icon.called(icon).of(Color.matchString(color)), text, value);
    }

    public AccordionElementContentBuilder addRowBold(Icon.Builder iconBuilder, String text, Serializable value) {
        return addRowBold(iconBuilder.build(), text, value);
    }

    public AccordionElementContentBuilder addRowBold(Icon icon, String text, Serializable value) {
        html.append("<p>").append(icon).append(" ").append(text);
        html.append("<span class=\"pull-right\"><b>").append(value).append("</b></span></p>");
        return this;
    }

    @Deprecated
    public AccordionElementContentBuilder addRow(String color, String icon, String text, Serializable value) {
        return addRow(Icon.called(icon).of(Color.matchString(color)), text, value);
    }

    public AccordionElementContentBuilder addRow(Icon.Builder iconBuilder, String text, Serializable value) {
        return addRow(iconBuilder.build(), text, value);
    }

    public AccordionElementContentBuilder addRow(Icon icon, String text, Serializable value) {
        html.append("<p>").append(icon).append(" ").append(text);
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
