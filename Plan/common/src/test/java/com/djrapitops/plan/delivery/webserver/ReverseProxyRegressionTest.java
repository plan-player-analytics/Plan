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

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.gathering.domain.DataMap;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import com.djrapitops.plan.storage.file.PlanFiles;
import extension.FullSystemExtension;
import extension.SeleniumExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.TestResources;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.djrapitops.plan.delivery.export.ExportTestUtilities.*;

/**
 * Tests against reverse proxy regression issues when using subdirectory (eg. /plan/...).
 *
 * @author AuroraLS3
 */
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(SeleniumExtension.class)
@ExtendWith(FullSystemExtension.class)
@Disabled("Docker networking doesn't work in Github Actions")
class ReverseProxyRegressionTest {

    private static final int PLAN_PORT = 9001;

    public static GenericContainer<?> webserver;
    private static ServerUUID serverUUID;

    @BeforeAll
    static void setUp(PlanFiles files, PlanConfig config, PlanSystem system) throws URISyntaxException, FileNotFoundException {
        Network network = Network.newNetwork(); // Define a network so that host.docker.internal resolution works.

        Path nginxConfig = files.getDataDirectory().resolve("nginx.conf");
        webserver = new GenericContainer<>(DockerImageName.parse("nginx:latest"))
                .withExposedPorts(80)
                .withNetwork(network)
                .withNetworkAliases("foo")
                .withExtraHost("host.docker.internal", "host-gateway")
                .withFileSystemBind(nginxConfig.toFile().getAbsolutePath(), "/etc/nginx/conf.d/default.conf")
                .waitingFor(new HttpWaitStrategy());
        TestResources.copyResourceToFile(nginxConfig.toFile(), new FileInputStream(TestResources.getTestResourceFile("nginx-reverse-proxy.conf", ReverseProxyRegressionTest.class)));
        webserver.start();

        config.set(PluginSettings.SERVER_NAME, "TestServer");
        config.set(WebserverSettings.PORT, PLAN_PORT);
        config.set(WebserverSettings.LOG_ACCESS_TO_CONSOLE, true);
        config.set(WebserverSettings.SHOW_ALTERNATIVE_IP, true);
        config.set(WebserverSettings.ALTERNATIVE_IP, webserver.getHost() + ":" + webserver.getMappedPort(80) + "/plan");
        // Avoid accidentally DDoS:ing head image service during tests.
        config.set(DisplaySettings.PLAYER_HEAD_IMG_URL, "data:image/png;base64,AA==");

        system.enable();
        serverUUID = system.getServerInfo().getServerUUID();
        savePlayerData(system.getDatabaseSystem().getDatabase(), serverUUID);
    }

    private static void savePlayerData(Database database, ServerUUID serverUUID) {
        UUID uuid = TestConstants.PLAYER_ONE_UUID;
        database.executeTransaction(new PlayerRegisterTransaction(uuid, RandomData::randomTime, TestConstants.PLAYER_ONE_NAME));
        FinishedSession session = new FinishedSession(uuid, serverUUID, 1000L, 11000L, 500L, new DataMap());
        database.executeTransaction(new StoreWorldNameTransaction(serverUUID, "world"));
        database.executeTransaction(new StoreSessionTransaction(session));
    }

    @AfterAll
    static void tearDown(PlanSystem system) {
        system.disable();
    }

    @AfterEach
    void clearBrowserConsole(WebDriver driver) {
        SeleniumExtension.newTab(driver);
    }

    @TestFactory
    Collection<DynamicTest> exportedWebpageDoesNotHaveErrors(ChromeDriver driver) {
        List<String> endpointsToTest = getEndpointsToTest(serverUUID);

        return endpointsToTest.stream().map(
                endpoint -> DynamicTest.dynamicTest("Reverse-proxied page does not log errors to js console /plan" + endpoint, () -> {
                    String address = "http://" + webserver.getHost() + ":" + webserver.getMappedPort(80) + "/plan"
                            + (endpoint.startsWith("/") ? endpoint : '/' + endpoint);

                    List<LogEntry> logs = getLogsAfterRequestToAddress(driver, address);
                    assertNoLogsExceptFaviconError(logs);
                })
        ).collect(Collectors.toList());
    }
}
