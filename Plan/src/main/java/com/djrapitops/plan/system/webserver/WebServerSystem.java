/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plugin.benchmarking.Timings;

import javax.inject.Inject;

/**
 * WebServer subsystem for managing WebServer initialization.
 *
 * @author Rsl1122
 */
public class WebServerSystem implements SubSystem {

    private final WebServer webServer;
    private Timings timings;

    @Inject
    public WebServerSystem(WebServer webServer, Timings timings) {
        this.webServer = webServer;
        this.timings = timings;
    }

    @Deprecated
    public static WebServerSystem getInstance() {
        return PlanSystem.getInstance().getWebServerSystem();
    }

    @Deprecated
    public static boolean isWebServerEnabled() {
        WebServer webServer = getInstance().webServer;
        return webServer != null && webServer.isEnabled();
    }

    @Override
    public void enable() throws EnableException {
        timings.start("WebServer Initialization");
        webServer.enable();
        timings.end("WebServer Initialization");
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
