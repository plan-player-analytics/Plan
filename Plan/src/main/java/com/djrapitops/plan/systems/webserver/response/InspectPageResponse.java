package main.java.com.djrapitops.plan.systems.webserver.response;

import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.webserver.theme.Theme;

import java.util.UUID;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class InspectPageResponse extends Response {

    private String inspectPagePluginsTab;

    public InspectPageResponse(InformationManager infoManager, UUID uuid) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(Theme.replaceColors(infoManager.getPlayerHtml(uuid)));
        setInspectPagePluginsTab(infoManager.getPluginsTabContent(uuid));
    }

    public void setInspectPagePluginsTab(String inspectPagePluginsTab) {
        if (this.inspectPagePluginsTab != null) {
            setContent(getContent().replace(this.inspectPagePluginsTab, inspectPagePluginsTab));
        } else {
            setContent(getContent().replace("${tabContentPlugins}", inspectPagePluginsTab));
        }
        this.inspectPagePluginsTab = inspectPagePluginsTab;
    }
}
