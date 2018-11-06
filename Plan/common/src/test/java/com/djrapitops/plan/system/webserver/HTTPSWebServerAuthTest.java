package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.DaggerPlanBukkitComponent;
import com.djrapitops.plan.PlanBukkitComponent;
import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.mocks.PlanBukkitMocker;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HTTPSWebServerAuthTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static PlanSystem bukkitSystem;

    @BeforeClass
    public static void setUpClass() throws Exception {
        PlanBukkitMocker mocker = PlanBukkitMocker.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withServer();
        PlanBukkitComponent component = DaggerPlanBukkitComponent.builder().plan(mocker.getPlanMock()).build();

        URL resource = HTTPSWebServerAuthTest.class.getResource("/Cert.keystore");
        String keyStore = resource.getPath();
        String absolutePath = new File(keyStore).getAbsolutePath();

        bukkitSystem = component.system();

        PlanConfig config = bukkitSystem.getConfigSystem().getConfig();

        config.set(Settings.WEBSERVER_CERTIFICATE_PATH, absolutePath);
        config.set(Settings.WEBSERVER_CERTIFICATE_KEYPASS, "MnD3bU5HpmPXag0e");
        config.set(Settings.WEBSERVER_CERTIFICATE_STOREPASS, "wDwwf663NLTm73gL");
        config.set(Settings.WEBSERVER_CERTIFICATE_ALIAS, "DefaultPlanCert");

        config.set(Settings.WEBSERVER_PORT, 9005);

        bukkitSystem.enable();

        bukkitSystem.getDatabaseSystem().getDatabase().save()
                .webUser(new WebUser("test", PassEncryptUtil.createHash("testPass"), 0));
    }

    @AfterClass
    public static void tearDownClass() {
        if (bukkitSystem != null) {
            bukkitSystem.disable();
        }
    }

    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
            }
    };

    private SSLSocketFactory getRelaxedSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        return sc.getSocketFactory();
    }

    /**
     * Test case against "Perm level 0 required, got 0".
     */
    @Test
    public void testHTTPSAuthForPages() throws IOException, WebException, KeyManagementException, NoSuchAlgorithmException {
        String address = "https://localhost:9005";
        URL url = new URL(address);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (address.startsWith("https")) {
            HttpsURLConnection httpsConn = (HttpsURLConnection) connection;

            // Disables unsigned certificate & hostname check, because we're trusting the user given certificate.
            // This allows https connections internally to local ports.
            httpsConn.setHostnameVerifier((hostname, session) -> true);
            httpsConn.setSSLSocketFactory(getRelaxedSocketFactory());
        }
        connection.setConnectTimeout(10000);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);

        String user = Base64Util.encode("test:testPass");
        connection.setRequestProperty("Authorization", "Basic " + user);

        int responseCode = connection.getResponseCode();

        switch (responseCode) {
            case 200:
            case 302:
                return;
            case 400:
                throw new BadRequestException("Bad Request: " + url.toString());
            case 403:
                throw new ForbiddenException(url.toString() + " returned 403");
            case 404:
                throw new NotFoundException(url.toString() + " returned a 404, ensure that your server is connected to an up to date Plan server.");
            case 412:
                throw new UnauthorizedServerException(url.toString() + " reported that it does not recognize this server. Make sure '/plan m setup' was successful.");
            case 500:
                throw new InternalErrorException();
            default:
                throw new WebException(url.toString() + "| Wrong response code " + responseCode);
        }
    }
}