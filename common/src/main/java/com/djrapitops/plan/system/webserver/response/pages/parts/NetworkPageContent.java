/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response.pages.parts;

import com.djrapitops.plan.system.webserver.response.Response;

import java.util.*;

/**
 * Represents Servers tab content of Network page.
 * <p>
 * Extends Response so that it can be stored in ResponseCache.
 *
 * @author Rsl1122
 */
public class NetworkPageContent extends Response {

    private final Map<String, String> content;

    public NetworkPageContent() {
        content = new HashMap<>();
    }

    public NetworkPageContent(String serverName, String html) {
        this();
        content.put(serverName, html);
    }

    public void addElement(String serverName, String html) {
        content.put(serverName, html);
    }

    public String getContents() {
        if (content.isEmpty()) {
            return "<div class=\"row clearfix\">" +
                    "<div class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12\">" +
                    "<div class=\"card\">" +
                    "<div class=\"header\">" +
                    "<div class=\"row clearfix\">" +
                    "<div class=\"col-xs-6 col-sm-6 col-lg-6\">" +
                    "<h2><i class=\"col-light-green fa fa-servers\"></i> No Servers</h2>" +
                    "</div>" +
                    "<div class=\"col-xs-6 col-sm-6 col-lg-6\">" +
                    "<a href=\"javascript:void(0)\" class=\"help material-icons pull-right\" " +
                    "data-trigger=\"focus\" data-toggle=\"popover\" data-placement=\"left\" " +
                    "data-container=\"body\" data-html=\"true\" " +
                    "data-original-title=\"No Servers\" " +
                    "data-content=\"This is displayed when no servers have sent server information to Bungee." +
                    "<br><br>You can try debugging the cause by using /planbungee con & /plan m con\"" +
                    ">help_outline</a></div></div></div>" +
                    "<div class=\"body\">" +
                    "<p>No Servers have sent information to Bungee.</p>" +
                    "</div></div></div></div>";
        }

        List<String> serverNames = new ArrayList<>(content.keySet());
        Collections.sort(serverNames);

        StringBuilder b = new StringBuilder();
        for (String serverName : serverNames) {
            b.append(content.get(serverName));
        }

        return b.toString();
    }
}
