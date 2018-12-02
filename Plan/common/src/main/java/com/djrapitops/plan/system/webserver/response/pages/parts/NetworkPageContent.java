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
package com.djrapitops.plan.system.webserver.response.pages.parts;

import com.djrapitops.plan.system.webserver.response.pages.PageResponse;

import java.util.*;

/**
 * Represents Servers tab content of Network page.
 * <p>
 * Extends Response so that it can be stored in ResponseCache.
 *
 * @author Rsl1122
 */
public class NetworkPageContent extends PageResponse {

    private final Map<String, String> content;

    public NetworkPageContent() {
        content = new HashMap<>();
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
