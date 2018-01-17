/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.response.pages.parts;

import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.utilities.html.HtmlStructure;

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