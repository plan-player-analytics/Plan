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
import com.djrapitops.plan.settings.config.paths.ProxySettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.SessionEndTransaction;
import com.djrapitops.plan.storage.database.transactions.events.WorldNameStoreTransaction;
import extension.SeleniumExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testcontainers.containers.NginxContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.mocks.PluginMockComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test class is for catching any JavaScript errors.
 * <p>
 * Errors may have been caused by:
 * - Missing placeholders {@code ${placeholder}} inside {@code <script>} tags.
 * - Automatic formatting of plugin javascript (See https://github.com/plan-player-analytics/Plan/issues/820)
 * - Missing file definition in Mocker
 */
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(SeleniumExtension.class)
class ExportJSErrorRegressionTest {

    static Path tempDir;
    static Path exportDirectory;

    static {
        try {
            tempDir = Files.createTempDirectory("export-test");
            exportDirectory = tempDir.resolve("export");

            Files.createDirectories(exportDirectory);
            Files.write(exportDirectory.resolve("index.html"), new byte[1]);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static PluginMockComponent component;

    @Container
    public static NginxContainer<?> nginx = new NginxContainer<>("nginx:latest")
            .withCustomContent(exportDirectory.toFile().getAbsolutePath())
            .waitingFor(new HttpWaitStrategy());
    private static PlanSystem planSystem;
    private static ServerUUID serverUUID;

    @BeforeAll
    static void setUpClass() throws Exception {
        component = new PluginMockComponent(tempDir);
        planSystem = component.getPlanSystem();

        PlanConfig config = planSystem.getConfigSystem().getConfig();
        config.set(WebserverSettings.DISABLED, true);
        config.set(ProxySettings.IP, "localhost");

        config.set(ExportSettings.HTML_EXPORT_PATH, exportDirectory.toFile().getAbsolutePath());
        config.set(ExportSettings.PLAYER_PAGES, true);
        config.set(ExportSettings.SERVER_PAGE, true);
        config.set(ExportSettings.PLAYERS_PAGE, true);

        planSystem.enable();
        serverUUID = planSystem.getServerInfo().getServerUUID();
        savePlayerData();

        Exporter exporter = planSystem.getExportSystem().getExporter();
        exporter.exportServerPage(planSystem.getServerInfo().getServer());
        exporter.exportPlayerPage(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME);
        exporter.exportPlayersPage();

        System.out.println("Exported files: \n");
        try (Stream<Path> walk = Files.walk(exportDirectory)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .map(File::getAbsolutePath)
                    .forEach(System.out::println);
        }
        System.out.println("Now running tests \n");
    }

    private static void savePlayerData() {
        DBSystem dbSystem = planSystem.getDatabaseSystem();
        Database database = dbSystem.getDatabase();
        UUID uuid = TestConstants.PLAYER_ONE_UUID;
        database.executeTransaction(new PlayerRegisterTransaction(uuid, RandomData::randomTime, TestConstants.PLAYER_ONE_NAME));
        FinishedSession session = new FinishedSession(uuid, serverUUID, 1000L, 11000L, 500L, new DataMap());
        database.executeTransaction(new WorldNameStoreTransaction(serverUUID, "world"));
        database.executeTransaction(new SessionEndTransaction(session));
    }

    @AfterAll
    static void tearDownClass() throws IOException {
        if (planSystem != null) {
            planSystem.disable();
        }

        try (Stream<Path> walk = Files.walk(tempDir)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .map(File::delete)
                    .forEach(Assertions::assertTrue);
        }
        Files.deleteIfExists(tempDir);
    }

    @AfterEach
    void tearDownTest(WebDriver driver) {
        SeleniumExtension.newTab(driver);
    }

    @TestFactory
    Collection<DynamicTest> exportedWebpageDoesNotHaveErrors(ChromeDriver driver) {
        String[] endpointsToTest = new String[]{
                "/player/" + TestConstants.PLAYER_ONE_UUID_STRING + "/index.html",
//                "/network/index.html",
                "/server/index.html",
                "/players/index.html"
        };

        return Arrays.stream(endpointsToTest).map(
                endpoint -> DynamicTest.dynamicTest("Exported page does not log errors to js console " + endpoint, () -> {
                    String address = nginx.getBaseUrl("http", 80).toURI().resolve(endpoint).toString();

                    // Avoid accidentally DDoS:ing head image service during tests.
                    planSystem.getConfigSystem().getConfig()
                            .set(DisplaySettings.PLAYER_HEAD_IMG_URL, nginx.getBaseUrl("http", 80).toURI()
                                    .resolve("/img/Flaticon_circle.png").toString());

                    driver.get(address);

                    List<LogEntry> logs = new ArrayList<>();
                    logs.addAll(driver.manage().logs().get(LogType.CLIENT).getAll());
                    logs.addAll(driver.manage().logs().get(LogType.BROWSER).getAll());

                    assertNoLogs(logs);
                })
        ).collect(Collectors.toList());
    }

    private void assertNoLogs(List<LogEntry> logs) {
        List<String> loggedLines = logs.stream()
                .map(log -> "\n" + log.getLevel().getName() + " " + log.getMessage())
                .filter(line -> !line.contains("favicon.ico"))
                .collect(Collectors.toList());
        assertTrue(loggedLines.isEmpty(), () -> "Browser console included " + loggedLines.size() + " logs: " + loggedLines);
    }
}