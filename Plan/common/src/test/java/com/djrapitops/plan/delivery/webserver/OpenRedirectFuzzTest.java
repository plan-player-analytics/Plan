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
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.webserver.http.WebServer;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.transactions.commands.StoreWebUserTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import extension.SeleniumExtension;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import utilities.RandomData;
import utilities.TestResources;
import utilities.mocks.PluginMockComponent;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.djrapitops.plan.delivery.export.ExportTestUtilities.getElementById;
import static com.djrapitops.plan.delivery.export.ExportTestUtilities.getMainPageElement;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SeleniumExtension.class)
@Disabled("This test can take 10 minutes to run so it's not enabled on the CI")
class OpenRedirectFuzzTest implements HttpsServerTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    private static PlanSystem system;
    private static List<String> payloads;

    @BeforeAll
    static void setUpClass(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("TestCert.p12").toFile();
        File testCert = TestResources.getTestResourceFile("TestCert.p12", OpenRedirectFuzzTest.class);
        Files.copy(testCert.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String absolutePath = file.getAbsolutePath();

        PluginMockComponent component = new PluginMockComponent(tempDir);
        system = component.getPlanSystem();

        PlanConfig config = system.getConfigSystem().getConfig();

        config.set(WebserverSettings.CERTIFICATE_PATH, absolutePath);
        config.set(WebserverSettings.CERTIFICATE_KEYPASS, "test");
        config.set(WebserverSettings.CERTIFICATE_STOREPASS, "test");
        config.set(WebserverSettings.CERTIFICATE_ALIAS, "test");

        config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);

        system.enable();

        User user = new User("test", "console", null, PassEncryptUtil.createHash("testPass"), "admin", Collections.emptyList());
        system.getDatabaseSystem().getDatabase().executeTransaction(new StoreWebUserTransaction(user));

        loadPayloads();
    }

    private static void loadPayloads() throws Exception {
        File payloads = TestResources.getTestResourceFile("fuzzing/Open-Redirect-payloads.txt", OpenRedirectFuzzTest.class);
        try (Stream<String> lines = Files.lines(payloads.toPath())) {
            OpenRedirectFuzzTest.payloads = lines.toList();
        }
    }

    private static void waitForLoginPageToLoad(ChromeDriver driver, String address) {
        new WebDriverWait(driver, Duration.of(1, ChronoUnit.SECONDS)).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        Awaitility.await()
                .alias("Login page didn't open using payload " + address)
                .atMost(Duration.of(1, ChronoUnit.SECONDS))
                .until(() -> getElementById(driver, "inputUser").map(WebElement::isDisplayed).orElse(false));
    }

    @AfterAll
    static void tearDownClass() {
        if (system != null) {
            system.disable();
        }
    }

    @AfterEach
    void clearBrowserConsole(WebDriver driver) {
        SeleniumExtension.newTab(driver);
    }

    @TestFactory
    Collection<DynamicTest> openRedirectFuzzTest(ChromeDriver driver) {
        assertFalse(payloads.isEmpty());

        int testPortNumber = testPortNumber();
        return payloads.stream()
                .map(payload -> payload.replace("$PORT", Integer.toString(testPortNumber)))
                .flatMap(payload -> Stream.of(payload, URLEncoder.encode(payload, StandardCharsets.UTF_8)))
                .map(payload -> DynamicTest.dynamicTest("Login has no open redirect vulnerability '" + payload + "'", () -> {
                    String address = "https://localhost:" + testPortNumber;
                    try {
                        String loginPageAddress = address + "/login?from=" + payload;

                        driver.get(loginPageAddress);

                        waitForLoginPageToLoad(driver, loginPageAddress);

                        driver.findElement(By.id("inputUser")).sendKeys("test");
                        driver.findElement(By.id("inputPassword")).sendKeys("testPass");
                        driver.findElement(By.id("login-button")).click();

                        try {
                            Awaitility.await()
                                    .atMost(Duration.of(1, ChronoUnit.SECONDS))
                                    .until(() -> getMainPageElement(driver).map(WebElement::isDisplayed).orElse(false));
                        } catch (ConditionTimeoutException e) {
                            String currentUrl = driver.getCurrentUrl();
                            assertTrue(currentUrl.startsWith(address), () -> payload + " redirected to " + currentUrl + " which should not have happened!");
                        }

                        String currentUrl = driver.getCurrentUrl();
                        assertTrue(currentUrl.startsWith(address), () -> payload + " redirected to " + currentUrl + " which should not have happened!");
                    } finally {
                        driver.get(address + "/auth/logout");
                    }
                }))
                .toList();
    }

    @Override
    public WebServer getWebServer() {
        return system.getWebServerSystem().getWebServer();
    }

    @Override
    public int testPortNumber() {
        return TEST_PORT_NUMBER;
    }
}
