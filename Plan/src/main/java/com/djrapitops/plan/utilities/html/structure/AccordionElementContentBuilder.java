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
