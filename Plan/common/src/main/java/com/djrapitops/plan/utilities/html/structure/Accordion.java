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

import com.djrapitops.plugin.utilities.Format;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for creating html accordions.
 *
 * @author Rsl1122
 */
public class Accordion {

    private final String id;
    private final List<AccordionElement> elements;

    private String emptyText = "No Data";

    public Accordion(String id) {
        this.id = new Format(id)
                .removeSymbols()
                .removeWhitespace()
                .toString();
        elements = new ArrayList<>();
    }

    public String toHtml() {
        StringBuilder html = new StringBuilder();

        html.append("<div class=\"panel-group scrollbar\" id=\"").append(id).append("\" role=\"tablist\" aria-multiselectable=\"true\">");

        if (elements.isEmpty()) {
            return "<div class=\"body\"><p>" + emptyText + "</p></div>";
        } else {
            for (AccordionElement element : elements) {
                html.append(element.toHtml());
            }
        }

        return html.append("</div>").toString(); // Close panel-group scrollbar
    }

    public void addElement(AccordionElement element) {
        element.setParentId(id);
        elements.add(element);
    }

    protected void setEmptyText(String text) {
        emptyText = text;
    }
}
