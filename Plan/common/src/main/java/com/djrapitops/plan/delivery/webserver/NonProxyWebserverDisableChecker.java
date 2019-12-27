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

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import java.io.IOException;

/**
 * In charge of disabling Webserver if a Proxy server is detected in the database.
 *
 * @author Rsl1122
 */
public class NonProxyWebserverDisableChecker implements Runnable {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final WebServerSystem webServerSystem;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    public NonProxyWebserverDisableChecker(
            PlanConfig config,
            DBSystem dbSystem,
            WebServerSystem webServerSystem,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.webServerSystem = webServerSystem;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        if (config.isFalse(PluginSettings.PROXY_COPY_CONFIG)) return;

        dbSystem.getDatabase().query(ServerQueries.fetchProxyServerInformation()).ifPresent(proxy -> {
            logger.info("Proxy server detected in the database - Proxy Webserver address is '" + proxy.getWebAddress() + "'.");
            WebServer webServer = webServerSystem.getWebServer();

            if (webServer.isEnabled()) {
                disableWebserver(webServer);
            }
        });
    }

    private void disableWebserver(WebServer webServer) {
        logger.warn("Disabling Webserver on this server - You can override this behavior by setting '" + PluginSettings.PROXY_COPY_CONFIG.getPath() + "' to false.");
        webServer.disable();
        try {
            config.set(WebserverSettings.DISABLED, true);
            config.save();
            logger.warn("Note: Set '" + WebserverSettings.DISABLED.getPath() + "' to true");

        } catch (IOException e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }
}