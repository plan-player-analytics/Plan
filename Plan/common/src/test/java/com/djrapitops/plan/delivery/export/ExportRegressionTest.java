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
import com.djrapitops.plan.gathering.domain.DataMap;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.java.Lists;
import extension.FullSystemExtension;
import extension.SeleniumExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;
import utilities.RandomData;
import utilities.TestConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author AuroraLS3
 */
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(SeleniumExtension.class)
@ExtendWith(FullSystemExtension.class)
class ExportRegressionTest {

    public static GenericContainer<?> webserver;
    private static Path exportDir;
    private static ServerUUID serverUUID;

    @BeforeAll
    static void setUp(PlanFiles files, PlanConfig config, PlanSystem system) throws Exception {
        exportDir = files.getDataDirectory().resolve("export");
        Files.createDirectories(exportDir);
        System.out.println("Export to " + exportDir.toFile().getAbsolutePath());

        webserver = new GenericContainer<>(DockerImageName.parse("halverneus/static-file-server:latest"))
                .withExposedPorts(8080)
                .withFileSystemBind(exportDir.toFile().getAbsolutePath(), "/web")
                .waitingFor(new HttpWaitStrategy());
        webserver.start();

        config.set(PluginSettings.FRONTEND_BETA, true);
        config.set(WebserverSettings.DISABLED, true);
        config.set(WebserverSettings.EXTERNAL_LINK, "http://" + webserver.getHost() + ":" + webserver.getMappedPort(8080));
        // Avoid accidentally DDoS:ing head image service during tests.
        config.set(DisplaySettings.PLAYER_HEAD_IMG_URL, "data:image/png;base64,AA==");
        config.set(ExportSettings.HTML_EXPORT_PATH, exportDir.toFile().getAbsolutePath());
        config.set(ExportSettings.SERVER_PAGE, true);
        config.set(ExportSettings.PLAYERS_PAGE, true);
        config.set(ExportSettings.PLAYER_PAGES, true);

        system.enable();
        serverUUID = system.getServerInfo().getServerUUID();
        savePlayerData(system.getDatabaseSystem().getDatabase(), serverUUID);
        export(system.getExportSystem().getExporter(), system.getDatabaseSystem().getDatabase());
    }

    private static void export(Exporter exporter, Database database) throws Exception {
        exporter.exportReact();
        assertTrue(exporter.exportServerPage(database.query(ServerQueries.fetchServerMatchingIdentifier(serverUUID.toString()))
                .orElseThrow(AssertionError::new)));
        assertTrue(exporter.exportPlayersPage());
        assertTrue(exporter.exportPlayerPage(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME));
    }

    private static void savePlayerData(Database database, ServerUUID serverUUID) {
        UUID uuid = TestConstants.PLAYER_ONE_UUID;
        database.executeTransaction(new PlayerRegisterTransaction(uuid, RandomData::randomTime, TestConstants.PLAYER_ONE_NAME));
        FinishedSession session = new FinishedSession(uuid, serverUUID, 1000L, 11000L, 500L, new DataMap());
        database.executeTransaction(new StoreWorldNameTransaction(serverUUID, "world"));
        database.executeTransaction(new StoreSessionTransaction(session));
    }

    @AfterAll
    static void tearDown(PlanSystem system) throws IOException {
        system.disable();
        FileUtils.cleanDirectory(exportDir.toFile());
    }

    @AfterEach
    void clearExportDirectory(WebDriver driver) {
        SeleniumExtension.newTab(driver);
    }

    @TestFactory
    Collection<DynamicTest> exportedWebpageDoesNotHaveErrors(ChromeDriver driver) throws Exception {
        List<String> endpointsToTest = Lists.builder(String.class)
                .add("/")
                .addAll(ServerPageExporter.getRedirections(serverUUID))
                .addAll(PlayerPageExporter.getRedirections(TestConstants.PLAYER_ONE_UUID))
                .add("/players")
                .build();

        return endpointsToTest.stream().map(
                endpoint -> DynamicTest.dynamicTest("Exported page does not log errors to js console " + endpoint, () -> {

                    String address = "http://" + webserver.getHost() + ":" + webserver.getMappedPort(8080)
                            + (endpoint.startsWith("/") ? endpoint : '/' + endpoint);
                    System.out.println("GET: " + address);
                    driver.get(address);

                    new WebDriverWait(driver, Duration.of(10, ChronoUnit.SECONDS)).until(
                            webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
                    Awaitility.await()
                            .atMost(Duration.of(10, ChronoUnit.SECONDS))
                            .until(() -> getElement(driver).map(WebElement::isDisplayed).orElse(false));

                    List<LogEntry> logs = new ArrayList<>();
                    logs.addAll(driver.manage().logs().get(LogType.CLIENT).getAll());
                    logs.addAll(driver.manage().logs().get(LogType.BROWSER).getAll());

                    assertNoLogs(logs);
                })
        ).collect(Collectors.toList());
    }

    private Optional<WebElement> getElement(ChromeDriver driver) {
        try {
            return Optional.of(driver.findElement(By.className("load-in")));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    private void assertNoLogs(List<LogEntry> logs) {
        List<String> loggedLines = logs.stream()
                .map(log -> "\n" + log.getLevel().getName() + " " + log.getMessage())
                .toList();
        assertTrue(loggedLines.isEmpty(), () -> "Browser console included " + loggedLines.size() + " logs: " + loggedLines);
    }
}
