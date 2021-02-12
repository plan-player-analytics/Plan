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
import com.djrapitops.plan.delivery.web.ResourceService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * WebServer subsystem for managing WebServer initialization.
 *
 * @author Rsl1122
 */
@Singleton
public class WebServerSystem implements SubSystem {

    private final Addresses addresses;
    private final WebServer webServer;

    @Inject
    public WebServerSystem(
            Addresses addresses,
            WebServer webServer
    ) {
        this.addresses = addresses;
        this.webServer = webServer;
    }

    @Override
    public void enable() {
        webServer.enable();
        if (!webServer.isAuthRequired()) {
            ResourceService.getInstance().addStylesToResource("Plan", "error.html", ResourceService.Position.PRE_CONTENT, "./css/noauth.css");
            ResourceService.getInstance().addStylesToResource("Plan", "server.html", ResourceService.Position.PRE_CONTENT, "../css/noauth.css");
            ResourceService.getInstance().addStylesToResource("Plan", "player.html", ResourceService.Position.PRE_CONTENT, "../css/noauth.css");
            ResourceService.getInstance().addStylesToResource("Plan", "players.html", ResourceService.Position.PRE_CONTENT, "./css/noauth.css");
            ResourceService.getInstance().addStylesToResource("Plan", "network.html", ResourceService.Position.PRE_CONTENT, "./css/noauth.css");
            ResourceService.getInstance().addStylesToResource("Plan", "query.html", ResourceService.Position.PRE_CONTENT, "./css/noauth.css");
        }
        if (webServer.isEnabled()) {
            ResourceService.getInstance().addStylesToResource("Plan", "server.html", ResourceService.Position.PRE_CONTENT, "../css/querybutton.css");
            ResourceService.getInstance().addStylesToResource("Plan", "players.html", ResourceService.Position.PRE_CONTENT, "./css/querybutton.css");
            ResourceService.getInstance().addStylesToResource("Plan", "network.html", ResourceService.Position.PRE_CONTENT, "./css/querybutton.css");
        }
    }

    @Override
    public void disable() {
        webServer.disable();
    }

    public WebServer getWebServer() {
        return webServer;
    }

    public Addresses getAddresses() {
        return addresses;
    }
}
