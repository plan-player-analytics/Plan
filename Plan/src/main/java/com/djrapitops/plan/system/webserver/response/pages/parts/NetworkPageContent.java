/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
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
            return ""; // TODO "No Servers"
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