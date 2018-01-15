/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.webserver.pagecache.ResponseCache;
import com.djrapitops.plugin.api.Check;

/**
 * WebServer subsystem for managing WebServer initialization.
 *
 * @author Rsl1122
 */
public class WebServerSystem implements SubSystem {

    private WebServer webServer;

    public WebServerSystem() {
        webServer = new WebServer();
    }

    public static WebServerSystem getInstance() {
        return PlanSystem.getInstance().getWebServerSystem();
    }

    public static boolean isWebServerEnabled() {
        WebServer webServer = getInstance().webServer;
        return webServer != null && webServer.isEnabled();
    }

    @Override
    public void enable() throws EnableException {
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
        ResponseCache.clearCache();
        webServer.stop();
    }

    public WebServer getWebServer() {
        return webServer;
    }
}