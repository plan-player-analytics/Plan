/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html;

import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.icon.Icons;
import org.apache.commons.text.TextStringBuilder;

/**
 * Class for parsing layout components of the websites.
 *
 * @author Rsl1122
 */
public class HtmlStructure {

    public static String separateWithDots(String... elements) {
        TextStringBuilder builder = new TextStringBuilder();
        builder.appendWithSeparators(elements, " &#x2022; ");
        return builder.toString();
    }

    public static String createDotList(String... elements) {
        if (elements.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String element : elements) {
            if (element.isEmpty()) {
                continue;
            }
            builder.append("&#x2022; ");
            builder.append(element);
            builder.append("<br>");
        }
        return builder.toString();
    }

    public static String[] createInspectPageTabContentCalculating() {
        String tab = "<div class=\"tab\">" +
                "<div class=\"row clearfix\">" +
                "<div class=\"col-lg-12 col-md-12 col-sm-12 col-xs-12\">" +
                "<div class=\"card\">" +
                "<div class=\"header\"><h2><i class=\"fa fa-users\"></i> Plugin Data</h2></div>" +
                "<div class=\"body\">" +
                "<p><i class=\"fa fa-spin fa-refresh\"></i> Calculating Plugins tab, refresh (F5) shortly..</p>" +
                "</div></div>" +
                "</div></div></div>";
        return new String[]{"<li><a>Calculating... Refresh shortly</a></li>", tab};
    }

    public static String playerStatus(boolean online, boolean banned, boolean op) {
        StringBuilder html = new StringBuilder("<p>");
        if (online) {
            html.append(Icon.called("circle").of(Color.GREEN)).append(" Online");
        } else {
            html.append(Icon.called("circle").of(Color.RED)).append(" Offline");
        }
        html.append("</p>");
        if (op) {
            html.append("<p>").append(Icons.OPERATOR).append(" Operator</p>");
        }
        if (banned) {
            html.append("<p>").append(Icons.BANNED).append(" Banned</p>");
        }
        return html.toString();
    }
}
