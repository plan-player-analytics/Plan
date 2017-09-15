/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.universal.PingWebAPI;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.TestInit;

import static org.junit.Assert.assertTrue;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class WebAPITest {

    private WebServer webServer;

    @Before
    public void setUp() throws Exception {
        TestInit.initEmptyLocale();
        webServer = new WebServer(null);
        webServer.initServer();
        assertTrue(webServer.isEnabled());
    }

    @Test
    public void testPingWebAPI() throws WebAPIException {
        WebAPI api = webServer.getWebAPI().getAPI(PingWebAPI.class);
        api.sendRequest(webServer.getAccessAddress(), Plan.getServerUUID());
    }
}