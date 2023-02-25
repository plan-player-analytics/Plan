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
package com.djrapitops.plan.delivery.export;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.file.PlanFiles;
import extension.FullSystemExtension;
import extension.SeleniumExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.djrapitops.plan.delivery.export.ExportTestUtilities.*;

/**
 * Tests exported website when exported to /plan/...
 *
 * @author AuroraLS3
 */
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(SeleniumExtension.class)
@ExtendWith(FullSystemExtension.class)
class ExportSubdirRegressionTest {

    public static GenericContainer<?> webserver;
    private static Path exportDir;
    private static ServerUUID serverUUID;

    @BeforeAll
    static void setUp(PlanFiles files, PlanConfig config, PlanSystem system) throws Exception {
        exportDir = files.getDataDirectory().resolve("export");
        Files.createDirectories(exportDir.resolve("plan"));
        System.out.println("Export to " + exportDir.resolve("plan").toFile().getAbsolutePath());

        webserver = new GenericContainer<>(DockerImageName.parse("halverneus/static-file-server:latest"))
                .withExposedPorts(8080)
                .withFileSystemBind(exportDir.toFile().getAbsolutePath(), "/web")
                .waitingFor(new HttpWaitStrategy());
        webserver.start();

        config.set(WebserverSettings.DISABLED, true);
        config.set(WebserverSettings.EXTERNAL_LINK, "http://" + webserver.getHost() + ":" + webserver.getMappedPort(8080) + "/plan");
        // Avoid accidentally DDoS:ing head image service during tests.
        config.set(DisplaySettings.PLAYER_HEAD_IMG_URL, "data:image/png;base64,AA==");
        // Using .resolve("plan") here to export to /web/plan
        config.set(ExportSettings.HTML_EXPORT_PATH, exportDir.resolve("plan").toFile().getAbsolutePath());
        config.set(ExportSettings.SERVER_PAGE, true);
        config.set(ExportSettings.PLAYERS_PAGE, true);
        config.set(ExportSettings.PLAYER_PAGES, true);

        system.enable();
        serverUUID = system.getServerInfo().getServerUUID();
        savePlayerData(system.getDatabaseSystem().getDatabase(), serverUUID);
        export(system.getExportSystem().getExporter(), system.getDatabaseSystem().getDatabase(), serverUUID);
    }

    @AfterAll
    static void tearDown(PlanSystem system) throws IOException {
        system.disable();
        FileUtils.cleanDirectory(exportDir.toFile());
    }

    @AfterEach
    void clearBrowserConsole(WebDriver driver) {
        SeleniumExtension.newTab(driver);
    }

    @TestFactory
    Collection<DynamicTest> exportedWebpageDoesNotHaveErrors(ChromeDriver driver) {
        List<String> endpointsToTest = getEndpointsToTest(serverUUID);

        return endpointsToTest.stream().map(
                endpoint -> DynamicTest.dynamicTest("Exported page does not log errors to js console " + endpoint, () -> {
                    String address = "http://" + webserver.getHost() + ":" + webserver.getMappedPort(8080) + "/plan"
                            + (endpoint.startsWith("/") ? endpoint : '/' + endpoint);
                    List<LogEntry> logs = getLogsAfterRequestToAddress(driver, address);
                    assertNoLogsExceptFaviconError(logs);
                })
        ).collect(Collectors.toList());
    }
}
