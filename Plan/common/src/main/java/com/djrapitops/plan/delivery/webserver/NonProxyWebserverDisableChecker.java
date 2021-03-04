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
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;

import java.io.IOException;

/**
 * In charge of disabling Webserver if a Proxy server is detected in the database.
 *
 * @author AuroraLS3
 */
public class NonProxyWebserverDisableChecker implements Runnable {

    private final PlanConfig config;
    private final Addresses addresses;
    private final WebServerSystem webServerSystem;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    public NonProxyWebserverDisableChecker(
            PlanConfig config,
            Addresses addresses,
            WebServerSystem webServerSystem,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.config = config;
        this.addresses = addresses;
        this.webServerSystem = webServerSystem;
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    @Override
    public void run() {
        if (config.isFalse(PluginSettings.PROXY_COPY_CONFIG)) return;

        addresses.getProxyServerAddress().ifPresent(address -> {
            logger.info("Proxy server detected in the database - Proxy Webserver address is '" + address + "'.");
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
            errorLogger.log(L.WARN, e, ErrorContext.builder()
                    .whatToDo("Set '" + WebserverSettings.DISABLED.getPath() + "' to true manually.")
                    .related("Disabling webserver in config setting", WebserverSettings.DISABLED.getPath()).build());
        }
    }
}