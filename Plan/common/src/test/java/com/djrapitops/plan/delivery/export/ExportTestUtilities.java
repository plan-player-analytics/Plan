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

import com.djrapitops.plan.gathering.domain.DataMap;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import com.djrapitops.plan.utilities.java.Lists;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.WebDriverWait;
import utilities.RandomData;
import utilities.TestConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author AuroraLS3
 */
public class ExportTestUtilities {

    private ExportTestUtilities() {
        /* Static utility method class */
    }

    public static void assertNoLogs(List<LogEntry> logs, String endpoint) {
        List<String> loggedLines = logs.stream()
                .map(log -> "\n" + log.getLevel().getName() + " " + log.getMessage())
                .filter(ExportTestUtilities::ignoredLogLines).toList();
        assertTrue(loggedLines.isEmpty(), () -> "Loading " + endpoint + ", Browser console included " + loggedLines.size() + " logs: " + loggedLines);
    }

    private static boolean ignoredLogLines(String log) {
        return !StringUtils.containsAny(log,
                "fonts.gstatic.com", "fonts.googleapis.com", "cdn.jsdelivr.net", "manifest.json"
        );
    }

    public static void assertNoLogsExceptFaviconError(List<LogEntry> logs) {
        List<String> loggedLines = logs.stream()
                .map(log -> "\n" + log.getLevel().getName() + " " + log.getMessage())
                .filter(ExportTestUtilities::ignoredLogLines)
                .filter(log -> !log.contains("favicon.ico"))
                .toList();
        assertTrue(loggedLines.isEmpty(), () -> "Browser console included " + loggedLines.size() + " logs: " + loggedLines);
    }

    public static Optional<WebElement> getMainPageElement(ChromeDriver driver) {
        try {
            return Optional.of(driver.findElement(By.className("load-in")));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    public static Optional<WebElement> getElementById(ChromeDriver driver, String id) {
        try {
            return Optional.of(driver.findElement(By.id(id)));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    public static List<LogEntry> getLogsAfterRequestToAddress(ChromeDriver driver, String address) {
        System.out.println("GET: " + address);
        driver.get(address);

        new WebDriverWait(driver, Duration.of(10, ChronoUnit.SECONDS)).until(
                webDriver -> "complete".equals(((JavascriptExecutor) webDriver).executeScript("return document.readyState")));

        assertFalse(driver.findElement(By.tagName("body")).getText().contains("Bad Gateway"), "502 Bad Gateway, nginx could not reach Plan");

        Awaitility.await("waitForElementToBeVisible .load-in")
                .atMost(Duration.of(10, ChronoUnit.SECONDS))
                .until(() -> getMainPageElement(driver).map(WebElement::isDisplayed).orElse(false));

        List<LogEntry> logs = new ArrayList<>();
        logs.addAll(driver.manage().logs().get(LogType.CLIENT).getAll());
        logs.addAll(driver.manage().logs().get(LogType.BROWSER).getAll());
        return logs;
    }

    static void export(Exporter exporter, Database database, ServerUUID serverUUID) throws Exception {
        exporter.exportReact();
        assertTrue(exporter.exportServerPage(database.query(ServerQueries.fetchServerMatchingIdentifier(serverUUID.toString()))
                .orElseThrow(AssertionError::new)));
        assertTrue(exporter.exportPlayersPage());
        assertTrue(exporter.exportPlayerPage(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME));
    }

    static void savePlayerData(Database database, ServerUUID serverUUID) {
        UUID uuid = TestConstants.PLAYER_ONE_UUID;
        database.executeTransaction(new PlayerRegisterTransaction(uuid, RandomData::randomTime, TestConstants.PLAYER_ONE_NAME));
        FinishedSession session = new FinishedSession(uuid, serverUUID, 1000L, 11000L, 500L, new DataMap());
        database.executeTransaction(new StoreWorldNameTransaction(serverUUID, "world"));
        database.executeTransaction(new StoreSessionTransaction(session));
    }

    public static List<String> getEndpointsToTest(ServerUUID serverUUID) {
        return Lists.builder(String.class)
                .add("/")
                .addAll(ServerPageExporter.getRedirections(serverUUID))
                .addAll(PlayerPageExporter.getRedirections(TestConstants.PLAYER_ONE_UUID))
                .add("/players")
                .add("/theme-editor")
                .add("/theme-editor/new")
                .add("/theme-editor/delete")
                .build();
    }

    @SuppressWarnings("unused") // Test debugging method
    static void logExportDirectoryContents(Path directory) throws IOException {
        System.out.println("Contents of " + directory);
        try (Stream<Path> walk = Files.walk(directory)) {
            walk.forEach(System.out::println);
        }
    }

}
