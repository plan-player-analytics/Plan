/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plugin.utilities.Format;

/**
 * Represents a structural HTML element that has Tabs on the top.
 *
 * @author Rsl1122
 */
public class TabsElement {

    private final Tab[] tabs;

    public TabsElement(Tab... tabs) {
        this.tabs = tabs;
    }

    public String[] toHtml() {
        StringBuilder nav = new StringBuilder();
        StringBuilder content = new StringBuilder();

        nav.append("<ul class=\"nav nav-tabs tab-nav-right\" role=\"tablist\">");
        content.append("<div class=\"tab-content\">");
        boolean first = true;
        for (Tab tab : tabs) {
            String id = tab.getId();
            String navText = tab.getNavText();
            String contentHtml = tab.getContentHtml();

            nav.append("<li role=\"presentation\"").append(first ? " class=\"active\"" : "")
                    .append("><a href=\"#").append(id).append("\" data-toggle=\"tab\">")
                    .append(navText).append("</a></li>");
            content.append("<div role=\"tabpanel\" class=\"tab-pane fade").append(first ? " in active" : "")
                    .append("\" id=\"").append(id).append("\">")
                    .append(contentHtml).append("</div>");
            first = false;
        }
        content.append("</div>");
        nav.append("</ul>");

        return new String[]{nav.toString(), content.toString()};
    }

    public static class Tab {

        private final String navText;
        private final String contentHtml;

        public Tab(String navText, String contentHtml) {
            this.navText = navText;
            this.contentHtml = contentHtml;
        }

        public String getNavText() {
            return navText;
        }

        public String getContentHtml() {
            return contentHtml;
        }

        public String getId() {
            return "tab_" + new Format(navText).removeSymbols().removeWhitespace().lowerCase().toString();
        }
    }
}
