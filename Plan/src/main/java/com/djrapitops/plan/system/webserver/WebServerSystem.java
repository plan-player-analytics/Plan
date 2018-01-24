/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;

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
        Benchmark.start("WebServer Initialization");
        webServer.initServer();
        if (!webServer.isEnabled()) {
            if (Check.isBungeeAvailable()) {
                throw new EnableException("WebServer did not initialize!");
            }
            if (Settings.WEBSERVER_DISABLED.isTrue()) {
                Log.warn("WebServer was not initialized. (WebServer.DisableWebServer: true)");
            } else {
                Log.error("WebServer was not initialized successfully. Is the port (" + Settings.WEBSERVER_PORT.getNumber() + ") in use?");
            }
        }
        Benchmark.stop("Enable", "WebServer Initialization");
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