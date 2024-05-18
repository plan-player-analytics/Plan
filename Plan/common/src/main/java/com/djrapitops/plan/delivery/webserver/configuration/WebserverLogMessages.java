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

import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.InvalidPathException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class WebserverLogMessages {

    private final Formatters formatters;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;
    private final Locale locale;
    private final Addresses addresses;

    private final AtomicLong warnedAboutXForwardedSecurityIssue = new AtomicLong(0L);

    @Inject
    public WebserverLogMessages(Formatters formatters, PluginLogger logger, ErrorLogger errorLogger, Locale locale, Addresses addresses) {
        this.formatters = formatters;
        this.logger = logger;
        this.errorLogger = errorLogger;
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

    public void warnAboutWhitelistBlock(@Untrusted String accessAddress, @Untrusted String requestedURIString) {
        logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_IP_WHITELIST_BLOCK, accessAddress, requestedURIString));
    }

    public void infoWebserverEnabled(int port) {
        String address = addresses.getAccessAddress().orElse(addresses.getFallbackLocalhostAddress());
        logger.info(locale.getString(PluginLang.ENABLED_WEB_SERVER, port, address));
    }

    public void warnWebserverDisabledByConfig() {
        logger.info(locale.getString(PluginLang.ENABLE_NOTIFY_WEB_SERVER_DISABLED));
    }

    public void keystoreNotFoundError(InvalidPathException error, String keyStorePath) {
        String errorMessage = error.getMessage();
        logger.error("WebServer: Could not find Keystore: " + errorMessage);
        errorLogger.error(error, ErrorContext.builder()
                .whatToDo(errorMessage + ", Fix this path to point to a valid keystore file: " + keyStorePath)
                .related(keyStorePath).build());
    }

    public void authenticationNotPossible() {
        logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_HTTP));
        logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_HTTP_USER_AUTH));
    }

    public void authenticationUsingProxy() {
        logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_USING_PROXY_MODE));
    }

    public void invalidCertificate() {
        logger.warn(locale.getString(PluginLang.WEB_SERVER_FAIL_STORE_LOAD));
    }

    public void keystoreFileNotFound(String keyStorePath) {
        logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_NO_CERT_FILE, keyStorePath));
    }

    public void certificateExpiryIn(long expires) {
        logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_CERT_EXPIRE_DATE, formatters.yearLong().apply(expires)));
    }

    public void certificateExpiryIsNear(long timeMillisToExpiry) {
        if (timeMillisToExpiry > 0) {
            logger.warn(locale.getString(PluginLang.WEB_SERVER_NOTIFY_CERT_EXPIRE_DATE_SOON, formatters.timeAmount().apply(timeMillisToExpiry)));
        } else {
            logger.warn(locale.getString(PluginLang.WEB_SERVER_NOTIFY_CERT_EXPIRE_DATE_PASSED));
        }
    }

    public void invalidCertificateMissingAlias(String alias, String keystorePath) {
        logger.error(locale.getString(PluginLang.WEB_SERVER_NOTIFY_CERT_NO_SUCH_ALIAS, alias, keystorePath));
    }

    public void unableToLoadKeystore(Exception e, String keystorePath) {
        logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_STORE_LOAD));
        errorLogger.error(e, ErrorContext.builder()
                .whatToDo("Make sure the Certificate settings are correct / You can try remaking the keystore without -passin or -passout parameters.")
                .related(keystorePath).build());
    }

    public void wrongCertFileFormat() {
        logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_EMPTY_FILE));
    }

    public void keystoreLoadingError(Exception e) {
        errorLogger.error(e, ErrorContext.builder()
                .logErrorMessage()
                .whatToDo("Make sure the Certificate settings are correct / You can try remaking the keystore without -passin or -passout parameters.")
                .build());
    }
}
