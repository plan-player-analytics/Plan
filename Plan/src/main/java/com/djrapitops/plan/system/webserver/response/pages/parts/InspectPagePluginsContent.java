/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response.pages.parts;

import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.response.Response;
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
public class InspectPagePluginsContent extends Response {

    // ServerUUID, {nav, html}
    private final Map<UUID, String[]> pluginsTab;

    public InspectPagePluginsContent() {
        pluginsTab = new HashMap<>();
    }

    public InspectPagePluginsContent(UUID serverUUID, String nav, String html) {
        pluginsTab = new HashMap<>();
        addTab(serverUUID, nav, html);
    }

    public static InspectPagePluginsContent generateForThisServer(UUID playerUUID) {
        HookHandler hookHandler = HookHandler.getInstance();
        Map<PluginData, InspectContainer> containers = hookHandler.getInspectContainersFor(playerUUID);
        String serverName = ServerInfo.getServerName();
        String actualServerName = serverName.equals("Plan") ? "Server " + ServerInfo.getServerID() : serverName;
        if (containers.isEmpty()) {
            return new InspectPagePluginsContent(playerUUID, "<li><a>" + actualServerName + " (No Data)</a></li>",
                    "<div class=\"tab\"><div class=\"row clearfix\">" +
                            "<div class=\"col-md-12\">" + Html.CARD.parse("<p>No Data (" + actualServerName + ")</p>") +
                            "</div></div></div>");
        }

        String nav = "<li><a class=\"nav-button\" href=\"javascript:void(0)\">" + actualServerName + "</a></li>";

        StringBuilder tab = new StringBuilder();
        tab.append("<div class=\"tab\"><div class=\"row clearfix\">");

        List<PluginData> order = new ArrayList<>(containers.keySet());
        order.sort(new PluginDataNameComparator());

        for (PluginData pluginData : order) {
            InspectContainer container = containers.get(pluginData);
            AnalysisPluginsTabContentCreator.appendThird(pluginData, container, tab);
        }

        tab.append("</div></div>");

        return new InspectPagePluginsContent(ServerInfo.getServerUUID(), nav, tab.toString());
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
