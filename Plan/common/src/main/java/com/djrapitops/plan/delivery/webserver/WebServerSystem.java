/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.webserver;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.delivery.webserver.auth.ActiveCookieStore;
import com.djrapitops.plan.delivery.webserver.http.WebServer;
import com.djrapitops.plan.storage.file.PublicHtmlFiles;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * WebServer subsystem for managing WebServer initialization.
 *
 * @author AuroraLS3
 */
@Singleton
public class WebServerSystem implements SubSystem {

    private final Addresses addresses;
    private final ActiveCookieStore activeCookieStore;
    private final PublicHtmlFiles publicHtmlFiles;
    private final WebServer webServer;
    private final PluginLogger logger;

    @Inject
    public WebServerSystem(
            Addresses addresses,
            ActiveCookieStore activeCookieStore,
            PublicHtmlFiles publicHtmlFiles,
            WebServer webServer,
            PluginLogger logger) {
        this.addresses = addresses;
        this.activeCookieStore = activeCookieStore;
        this.publicHtmlFiles = publicHtmlFiles;
        this.webServer = webServer;
        this.logger = logger;
    }

    @Override
    public void enable() {
        activeCookieStore.enable();
        webServer.enable();

        if (publicHtmlFiles.findPublicHtmlResource("index.html").isPresent()) {
            logger.info("Found index.html in public_html, using a custom React bundle!");
        }
    }

    @Override
    public void disable() {
        webServer.disable();
        activeCookieStore.disable();
    }

    public WebServer getWebServer() {
        return webServer;
    }

    public Addresses getAddresses() {
        return addresses;
    }
}
