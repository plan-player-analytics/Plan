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

import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class InvalidConfigurationWarnings {

    private final PluginLogger logger;

    private final AtomicLong warnedAboutXForwardedSecurityIssue = new AtomicLong(0L);

    @Inject
    public InvalidConfigurationWarnings(PluginLogger logger) {
        this.logger = logger;
    }

    public void warnAboutXForwardedForSecurityIssue() {
        if (System.currentTimeMillis() - warnedAboutXForwardedSecurityIssue.get() > TimeUnit.MINUTES.toMillis(2L)) {
            logger.warn("Security Vulnerability due to misconfiguration: X-Forwarded-For header was not present in a request & '" +
                    WebserverSettings.IP_WHITELIST_X_FORWARDED.getPath() + "' is 'true'!");
            logger.warn("This could mean non-reverse-proxy access is not blocked & someone can use IP Spoofing to bypass security!");
            logger.warn("Make sure you can only access Plan panel from your reverse-proxy or disable this setting.");
            warnedAboutXForwardedSecurityIssue.set(System.currentTimeMillis());
        }
    }

}
