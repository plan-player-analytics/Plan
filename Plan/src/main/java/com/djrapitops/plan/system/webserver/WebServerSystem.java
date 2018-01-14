/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.systems.Systems;
import com.djrapitops.plugin.api.Check;

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
    public void enable() throws EnableException {
        webServer = new WebServer();
        webServer.initServer();
        if (Check.isBungeeAvailable() && !webServer.isEnabled()) {
            throw new EnableException("WebServer did not initialize!");
        }
        ResponseHandler responseHandler = webServer.getResponseHandler();
        responseHandler.registerWebAPIPages();
        responseHandler.registerDefaultPages();
    }

    @Override
    public void disable() {
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