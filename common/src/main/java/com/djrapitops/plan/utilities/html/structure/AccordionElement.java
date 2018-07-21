/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plugin.utilities.Format;
import com.djrapitops.plugin.utilities.Verify;

/**
 * Utility for creating html accordion elements.
 *
 * @author Rsl1122
 */
public class AccordionElement {

    private final String id;
    private final String title;
    private String color;
    private String parentId;
    private String leftSide;
    private String rightSide;

    public AccordionElement(String id, String title) {
        this.id = new Format(id)
                .removeSymbols()
                .removeWhitespace()
                .toString();
        this.title = title;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public AccordionElement setColor(String color) {
        this.color = color;
        return this;
    }

    public AccordionElement setLeftSide(String leftSide) {
        this.leftSide = leftSide;
        return this;
    }

    public AccordionElement setRightSide(String rightSide) {
        this.rightSide = rightSide;
        return this;
    }

    public String toHtml() {
        Verify.nullCheck(parentId, () -> new IllegalStateException("Parent ID not specified"));

        StringBuilder html = new StringBuilder();

        appendPanelHeading(html);
        appendLeftContent(html);
        appendRightContent(html);

        // Finalize content
        html.append("</div>") // Closes row clearfix
                .append("</div>") // Closes panel-body
                .append("</div>") // Closes panel-collapse
                .append("</div>"); // Closes panel

        return html.toString();
    }

    private void appendRightContent(StringBuilder html) {
        if (rightSide != null) {
            html.append("<div class=\"col-xs-12 col-sm-6 col-md-6 col-lg-6\">") // Right col-6
                    .append(rightSide)
                    .append("</div>"); // Right col-6
        }
    }

    private void appendLeftContent(StringBuilder html) {
        Verify.nullCheck(leftSide, () -> new IllegalStateException("No Content specified"));
        html.append("<div id=\"").append(id).append("\" class=\"panel-collapse collapse\" role=\"tabpanel\"")
                .append(" aria-labelledby=\"heading_").append(id).append("\">")
                .append("<div class=\"panel-body\"><div class=\"row clearfix\">")
                .append("<div class=\"col-xs-12 col-sm-6 col-md-6 col-lg-6\">") // Left col-6
                .append(leftSide)
                .append("</div>"); // Closes Left col-6
    }

    private void appendPanelHeading(StringBuilder html) {
        html.append("<div class=\"panel");
        if (color != null) {
            html.append(" panel-col-").append(color);
        }
        html.append("\">")
                .append("<div class=\"panel-heading\" role=\"tab\" id=\"heading_").append(id).append("\">")
                .append("<h4 class=\"panel-title\">")
                .append("<a class=\"collapsed\" role=\"button\" data-toggle=\"collapse\" data-parent=\"#")
                .append(parentId).append("\" ")
                .append("href=\"#").append(id).append("\" aria-expanded=\"false\" ")
                .append("aria-controls=\"").append(id).append("\">")
                .append(title) // Title (header)
                .append("</a></h4>") // Closes collapsed & panel title
                .append("</div>"); // Closes panel heading
    }
}
