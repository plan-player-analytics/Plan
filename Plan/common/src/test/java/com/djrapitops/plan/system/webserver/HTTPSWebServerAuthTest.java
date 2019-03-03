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
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.db.access.transactions.commands.RegisterWebUserTransaction;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import rules.ComponentMocker;
import rules.PluginComponentMocker;
import utilities.HTTPConnector;
import utilities.RandomData;
import utilities.TestResources;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HTTPSWebServerAuthTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static ComponentMocker component = new PluginComponentMocker(temporaryFolder);

    private static PlanSystem system;

    private HTTPConnector connector = new HTTPConnector();

    @BeforeClass
    public static void setUpClass() throws Exception {
        File file = temporaryFolder.newFile();
        TestResources.copyResourceIntoFile(file, "/Cert.keystore");
        String absolutePath = file.getAbsolutePath();

        system = component.getPlanSystem();

        PlanConfig config = system.getConfigSystem().getConfig();

        config.set(WebserverSettings.CERTIFICATE_PATH, absolutePath);
        config.set(WebserverSettings.CERTIFICATE_KEYPASS, "MnD3bU5HpmPXag0e");
        config.set(WebserverSettings.CERTIFICATE_STOREPASS, "wDwwf663NLTm73gL");
        config.set(WebserverSettings.CERTIFICATE_ALIAS, "DefaultPlanCert");

        config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);

        system.enable();

        WebUser webUser = new WebUser("test", PassEncryptUtil.createHash("testPass"), 0);
        system.getDatabaseSystem().getDatabase().executeTransaction(new RegisterWebUserTransaction(webUser));
    }

    @AfterClass
    public static void tearDownClass() {
        if (system != null) {
            system.disable();
        }
    }

    /**
     * Test case against "Perm level 0 required, got 0".
     */
    @Test(timeout = 5000)
//    @Ignore("HTTPS Start fails due to paths being bad for some reason")
    public void testHTTPSAuthForPages() throws IOException, WebException, KeyManagementException, NoSuchAlgorithmException {
        assertTrue("WebServer is not using https", system.getWebServerSystem().getWebServer().isUsingHTTPS());

        String address = "https://localhost:" + TEST_PORT_NUMBER;
        URL url = new URL(address);
        HttpURLConnection connection = connector.getConnection("GET", address);

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