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

import com.djrapitops.plan.delivery.webserver.http.WebServer;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import java.io.IOException;

/**
 * In charge of disabling Webserver if a Proxy server is detected in the database.
 *
 * @author AuroraLS3
 */
public class NonProxyWebserverDisableChecker implements Runnable {

    private final PlanConfig config;
    private final Locale locale;
    private final Addresses addresses;
    private final WebServerSystem webServerSystem;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    public NonProxyWebserverDisableChecker(
            PlanConfig config,
            Locale locale,
            Addresses addresses,
            WebServerSystem webServerSystem,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.config = config;
        this.locale = locale;
        this.addresses = addresses;
        this.webServerSystem = webServerSystem;
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    @Override
    public void run() {
        if (config.isFalse(PluginSettings.PROXY_COPY_CONFIG)) return;

        addresses.getProxyServerAddress().ifPresent(address -> {
            logger.info(locale.getString(PluginLang.ENABLE_NOTIFY_PROXY_ADDRESS, address));
            WebServer webServer = webServerSystem.getWebServer();

            if (webServer.isEnabled()) {
                disableWebserver(webServer);
            }
        });
    }

    private void disableWebserver(WebServer webServer) {
        logger.warn(locale.getString(PluginLang.ENABLE_NOTIFY_PROXY_DISABLED_WEBSERVER, PluginSettings.PROXY_COPY_CONFIG.getPath()));
        webServer.disable();
        try {
            config.set(WebserverSettings.DISABLED, true);
            config.save();
            logger.warn(locale.getString(PluginLang.ENABLE_NOTIFY_SETTING_CHANGE, WebserverSettings.DISABLED.getPath(), "true"));
        } catch (IOException e) {
            errorLogger.warn(e, ErrorContext.builder()
                    .whatToDo("Set '" + WebserverSettings.DISABLED.getPath() + "' to true manually.")
                    .related("Disabling webserver in config setting", WebserverSettings.DISABLED.getPath()).build());
        }
    }
}