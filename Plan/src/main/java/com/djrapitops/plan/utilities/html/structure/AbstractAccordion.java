/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
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
public class AbstractAccordion {

    private final String id;
    private final List<AccordionElement> elements;

    private String emptyText = "No Data";

    public AbstractAccordion(String id) {
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
