/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plugin.benchmarking.Timings;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * WebServer subsystem for managing WebServer initialization.
 *
 * @author Rsl1122
 */
@Singleton
public class WebServerSystem implements SubSystem {

    private final WebServer webServer;
    private Timings timings;

    @Inject
    public WebServerSystem(WebServer webServer, Timings timings) {
        this.webServer = webServer;
        this.timings = timings;
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
