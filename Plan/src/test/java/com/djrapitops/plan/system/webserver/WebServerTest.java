package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.Plan;
import com.sun.net.httpserver.HttpExchange;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import utilities.MockUtils;
import utilities.TestInit;

import java.io.IOException;
import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class})
public class WebServerTest {

    private WebServer webServer;
    private RequestHandler requestHandler;

    @Before
    public void setUp() throws Exception {
        TestInit testInit = TestInit.init();
        Plan plugin = testInit.getPlanMock();
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