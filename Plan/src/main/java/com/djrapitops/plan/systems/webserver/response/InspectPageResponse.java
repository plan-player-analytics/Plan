package main.java.com.djrapitops.plan.systems.webserver.response;

import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.webserver.theme.Theme;
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

    public InspectPageResponse(InformationManager infoManager, UUID uuid) {
        this.uuid = uuid;
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(infoManager.getPlayerHtml(uuid));
        setInspectPagePluginsTab(infoManager.getPluginsTabContent(uuid));
    }

    public InspectPageResponse(InformationManager infoManager, UUID uuid, String html) {
        this.uuid = uuid;
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(Theme.replaceColors(html));
        setInspectPagePluginsTab(infoManager.getPluginsTabContent(uuid));
    }

    private InspectPageResponse(InspectPageResponse response) {
        this.uuid = response.uuid;
        super.setHeader(response.getHeader());
        super.setContent(response.getContent());
    }

    public void setInspectPagePluginsTab(String[] inspectPagePluginsTab) {
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("navPluginsTabs", inspectPagePluginsTab[0]);
        replaceMap.put("pluginsTabs", inspectPagePluginsTab[1]);

        setContent(StrSubstitutor.replace(getContent(), replaceMap));
    }

    public static InspectPageResponse copyOf(InspectPageResponse response) {
        return new InspectPageResponse(response);
    }
}
