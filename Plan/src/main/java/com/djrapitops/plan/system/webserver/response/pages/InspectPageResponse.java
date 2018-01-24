package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.pages.parts.InspectPagePluginsContent;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class InspectPageResponse extends Response {

    private final UUID uuid;
    private InspectPagePluginsContent pluginsTab;

    public InspectPageResponse(UUID uuid, String html) {
        this.uuid = uuid;
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(Theme.replaceColors(html));
        pluginsTab = (InspectPagePluginsContent)
                ResponseCache.loadResponse(PageId.PLAYER_PLUGINS_TAB.of(uuid), InspectPagePluginsContent::new);
    }

    private InspectPageResponse(InspectPageResponse response) {
        this.uuid = response.uuid;
        super.setHeader(response.getHeader());
        super.setContent(response.getContent());
    }

    public static InspectPageResponse copyOf(InspectPageResponse response) {
        return new InspectPageResponse(response);
    }

    @Override
    public String getContent() {
        Map<String, String> replaceMap = new HashMap<>();
        String[] inspectPagePluginsTab = pluginsTab.getContents();
        replaceMap.put("navPluginsTabs", inspectPagePluginsTab[0]);
        replaceMap.put("pluginsTabs", inspectPagePluginsTab[1]);

        return StrSubstitutor.replace(super.getContent(), replaceMap);
    }

}
