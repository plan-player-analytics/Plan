package main.java.com.djrapitops.plan.systems.webserver.response;

import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.webserver.theme.Theme;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class AnalysisPageResponse extends Response {

    public AnalysisPageResponse(InformationManager informationManager) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(informationManager.getAnalysisHtml());
    }

    public AnalysisPageResponse(String html) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(Theme.replaceColors(html));
    }
}
