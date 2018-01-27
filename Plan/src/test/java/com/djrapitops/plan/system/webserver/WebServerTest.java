package com.djrapitops.plan.system.webserver;

import com.sun.net.httpserver.HttpExchange;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.mocks.SystemMockUtil;
import utilities.mocks.objects.MockUtils;

import java.io.IOException;
import java.util.HashMap;

@RunWith(MockitoJUnitRunner.Silent.class)
public class WebServerTest {

    private WebServer webServer;
    private RequestHandler requestHandler;

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        SystemMockUtil.setUp(temporaryFolder.getRoot())
                .enableConfigSystem();
    }

    @Before
    public void setUp() {
        webServer = new WebServer();
        requestHandler = new RequestHandler(webServer);
    }

    @Test
    public void testMockSetup() throws IOException {
        HttpExchange exchange = MockUtils.getHttpExchange(
                "POST",
                "/api/pingwebapi",
                "",
                new HashMap<>()
        );
        requestHandler.handle(exchange);
        System.out.println(MockUtils.getResponseStream(exchange));
    }
}