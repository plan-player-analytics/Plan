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
package com.djrapitops.plan.delivery.webserver.configuration;

import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class WebserverLogMessages {

    private final PluginLogger logger;
    private final Locale locale;
    private final Addresses addresses;

    private final AtomicLong warnedAboutXForwardedSecurityIssue = new AtomicLong(0L);

    @Inject
    public WebserverLogMessages(PluginLogger logger, Locale locale, Addresses addresses) {
        this.logger = logger;
        this.locale = locale;
        this.addresses = addresses;
    }

    public void warnAboutXForwardedForSecurityIssue() {
        if (System.currentTimeMillis() - warnedAboutXForwardedSecurityIssue.get() > TimeUnit.MINUTES.toMillis(2L)) {
            logger.warn("Security Vulnerability due to misconfiguration: X-Forwarded-For header was not present in a request & '" +
                    WebserverSettings.IP_USE_X_FORWARDED_FOR.getPath() + "' is 'true'!");
            logger.warn("This could mean non-reverse-proxy access is not blocked & someone can use IP Spoofing to bypass security!");
            logger.warn("Make sure you can only access Plan panel from your reverse-proxy or disable this setting.");
            warnedAboutXForwardedSecurityIssue.set(System.currentTimeMillis());
        }
    }

    public void warnAboutWhitelistBlock(String accessAddress, String requestedURIString) {
        logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_IP_WHITELIST_BLOCK, accessAddress, requestedURIString));
    }

    public void infoWebserverEnabled(int port) {
        String address = addresses.getAccessAddress().orElse(addresses.getFallbackLocalhostAddress());
        logger.info(locale.getString(PluginLang.ENABLED_WEB_SERVER, port, address));
    }

    public void warnWebserverDisabledByConfig() {
        logger.warn(locale.getString(PluginLang.ENABLE_NOTIFY_WEB_SERVER_DISABLED));
    }
}
