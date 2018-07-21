package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.system.webserver.response.pages.parts.InspectPagePluginsContent;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class InspectPageResponse extends Response {

    private final UUID uuid;

    public InspectPageResponse(UUID uuid, String html) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(Theme.replaceColors(html));
        this.uuid = uuid;
    }

    public static InspectPageResponse getRefreshing() {
        ErrorResponse refreshPage = new ErrorResponse();
        refreshPage.setTitle("Player page request is being processed..");
        refreshPage.setParagraph("<meta http-equiv=\"refresh\" content=\"2\" /><i class=\"fa fa-refresh fa-spin\" aria-hidden=\"true\"></i> Page will refresh automatically..");
        refreshPage.replacePlaceholders();
        return new InspectPageResponse(null, refreshPage.getContent());
    }

    @Override
    public String getContent() {
        Map<String, String> replaceMap = new HashMap<>();
        InspectPagePluginsContent pluginsTab = (InspectPagePluginsContent)
                ResponseCache.loadResponse(PageId.PLAYER_PLUGINS_TAB.of(uuid));
        String[] inspectPagePluginsTab = pluginsTab != null ? pluginsTab.getContents() : getCalculating();
        replaceMap.put("navPluginsTabs", inspectPagePluginsTab[0]);
        replaceMap.put("pluginsTabs", inspectPagePluginsTab[1]);

        return StringSubstitutor.replace(super.getContent(), replaceMap);
    }

    private String[] getCalculating() {
        return new String[]{"<li><i class=\"fa fa-spin fa-refresh\"></i><a> Calculating...</a></li>", ""};
    }
}
