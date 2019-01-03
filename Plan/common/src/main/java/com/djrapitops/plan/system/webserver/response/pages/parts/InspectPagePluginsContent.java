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

import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.response.pages.PageResponse;
import com.djrapitops.plan.utilities.comparators.PluginDataNameComparator;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plan.utilities.html.structure.AnalysisPluginsTabContentCreator;

import java.util.*;

/**
 * Represents Plugins tabs on Inspect page.
 * <p>
 * Extends Response so that it can be stored in ResponseCache.
 *
 * @author Rsl1122
 */
public class InspectPagePluginsContent extends PageResponse {

    // ServerUUID, {nav, html}
    private final Map<UUID, String[]> pluginsTab;

    public InspectPagePluginsContent() {
        pluginsTab = new HashMap<>();
    }

    public InspectPagePluginsContent(UUID serverUUID, String nav, String html) {
        pluginsTab = new HashMap<>();
        addTab(serverUUID, nav, html);
    }

    public static InspectPagePluginsContent generateForThisServer(UUID playerUUID, ServerInfo serverInfo, HookHandler hookHandler) {
        String serverName = serverInfo.getServer().getName();
        String actualServerName = "Plan".equals(serverName) ? "Server " + serverInfo.getServer().getId() : serverName;

        Map<PluginData, InspectContainer> containers = hookHandler.getInspectContainersFor(playerUUID);
        if (containers.isEmpty()) {
            return new InspectPagePluginsContent(playerUUID, "<li><a>" + actualServerName + " (No Data)</a></li>",
                    "<div class=\"tab\"><div class=\"row clearfix\">" +
                            "<div class=\"col-md-12\">" + Html.CARD.parse("<p>No Data (" + actualServerName + ")</p>") +
                            "</div></div></div>");
        }

        String nav = "<li><a class=\"nav-button\" href=\"javascript:void(0)\">" + actualServerName + "</a></li>";
        String tab = createTab(containers);

        return new InspectPagePluginsContent(serverInfo.getServerUUID(), nav, tab);
    }

    private static String createTab(Map<PluginData, InspectContainer> containers) {
        StringBuilder tab = new StringBuilder();
        tab.append("<div class=\"tab\"><div class=\"row clearfix\">");

        List<PluginData> order = new ArrayList<>(containers.keySet());
        order.sort(new PluginDataNameComparator());

        for (PluginData pluginData : order) {
            InspectContainer container = containers.get(pluginData);
            AnalysisPluginsTabContentCreator.appendThird(pluginData, container, tab);
        }

        tab.append("</div></div>");
        return tab.toString();
    }

    public void addTab(UUID serverUUID, String nav, String html) {
        pluginsTab.put(serverUUID, new String[]{nav, html});
    }

    public void addTab(InspectPagePluginsContent content) {
        pluginsTab.putAll(content.pluginsTab);
    }

    public String[] getContents() {
        if (pluginsTab.isEmpty()) {
            return HtmlStructure.createInspectPageTabContentCalculating();
        }

        List<String[]> order = new ArrayList<>(pluginsTab.values());
        // Sort serverNames alphabetically
        order.sort(Comparator.comparing(name -> name[0]));

        StringBuilder nav = new StringBuilder();
        StringBuilder tabs = new StringBuilder();
        for (String[] tab : order) {
            nav.append(tab[0]);
            tabs.append(tab[1]);
        }
        return new String[]{nav.toString(), tabs.toString()};
    }
}
