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
package com.djrapitops.plan.delivery.webserver.http;

import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Optional;

@Singleton
public class LegacyJettySSLContextLoader {

    private final Locale locale;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    @Inject
    public LegacyJettySSLContextLoader(Locale locale, PluginLogger logger, ErrorLogger errorLogger) {
        this.locale = locale;
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    public Optional<SslContextFactory.Server> load(String keyStorePath, String storepass, String keypass, String alias) {
        String keyStoreKind = keyStorePath.endsWith(".p12") ? "PKCS12" : "JKS";
        try (FileInputStream fIn = new FileInputStream(keyStorePath)) {
            KeyStore keystore = KeyStore.getInstance(keyStoreKind);

            keystore.load(fIn, storepass.toCharArray());
            Certificate cert = keystore.getCertificate(alias);

            if (cert == null) {
                throw new IllegalStateException("Alias: '" + alias + "' was not found in file " + keyStorePath + ".");
            }

            logger.info("Certificate: " + cert.getType());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keystore, keypass.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keystore);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagerFactory.getKeyManagers(), null/*trustManagerFactory.getTrustManagers()*/, null);

            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setSslContext(sslContext);

            return Optional.of(sslContextFactory);
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_SSL_CONTEXT));
            errorLogger.error(e, ErrorContext.builder().related(keyStoreKind).build());
        } catch (EOFException e) {
            logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_EMPTY_FILE));
        } catch (FileNotFoundException e) {
            logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_NO_CERT_FILE, keyStorePath));
            logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_HTTP));
        } catch (IOException e) {
            errorLogger.error(e, ErrorContext.builder().related(keyStorePath).build());
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException e) {
            logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_STORE_LOAD));
            errorLogger.error(e, ErrorContext.builder()
                    .whatToDo("Make sure the Certificate settings are correct / You can try remaking the keystore without -passin or -passout parameters.")
                    .related(keyStorePath).build());
        }
        return Optional.empty();
    }

}
