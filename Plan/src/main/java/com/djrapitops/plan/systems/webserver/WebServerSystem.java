/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver;

import com.djrapitops.plugin.api.Check;
import main.java.com.djrapitops.plan.api.exceptions.PlanEnableException;
import main.java.com.djrapitops.plan.systems.SubSystem;
import main.java.com.djrapitops.plan.systems.Systems;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class WebServerSystem implements SubSystem {

    private WebServer webServer;

    public WebServerSystem() {

    }

    public static WebServerSystem getInstance() {
        return Systems.getInstance().getWebServerSystem();
    }

    @Override
    public void init() throws PlanEnableException {
        webServer = new WebServer(MiscUtils.getIPlan());
        webServer.initServer();
        if (Check.isBungeeAvailable() && !webServer.isEnabled()) {
            throw new PlanEnableException("WebServer did not initialize!");
        }
    }

    @Override
    public void close() {
        // TODO Remove after WebServer setting requirement is gone.
        if (webServer != null) {
            webServer.stop();
        }
    }

    public static boolean isWebServerEnabled() {
        WebServer webServer = getInstance().webServer;
        return webServer != null && webServer.isEnabled();
    }

    public WebServer getWebServer() {
        return webServer;
    }
}