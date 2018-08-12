/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plugin.api.Benchmark;

import java.util.function.Supplier;

/**
 * WebServer subsystem for managing WebServer initialization.
 *
 * @author Rsl1122
 */
public class WebServerSystem implements SubSystem {

    private WebServer webServer;

    public WebServerSystem(Supplier<Locale> locale) {
        webServer = new WebServer(locale);
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
        webServer.enable();
        ResponseHandler responseHandler = webServer.getResponseHandler();
        responseHandler.registerWebAPIPages();
        responseHandler.registerDefaultPages();
        Benchmark.stop("Enable", "WebServer Initialization");
    }

    @Override
    public void disable() {
        ResponseCache.clearCache();
        webServer.disable();
    }

    public WebServer getWebServer() {
        return webServer;
    }
}
