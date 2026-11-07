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
 *  MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package utilities;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import extension.SeleniumExtension;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import utilities.dagger.PlanPluginComponent;
import utilities.mocks.PluginMockComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

/**
 * Utility to take a screenshot of the contributors section inside the information modal
 * and save it to the project root.
 * <p>
 * Similar to {@link LocaleResourceUpdateUtility}, this utility sets up a full Plan system,
 * starts a web server, uses Selenium to open the information modal, and captures a screenshot
 * of the contributors element.
 *
 * @author AuroraLS3
 */
public class ContributorsScreenshotUtility {

    private static final int TEST_PORT_NUMBER = 9092;

    public static void main(String[] args) throws IOException {
        // Set up ChromeDriver
        ChromeDriver driver = createChromeDriver();
        // Set up Plan system
        PlanSystem system = setupSystem();

        try {
            system.enable();

            // Navigate to the page
            String address = "http://localhost:" + TEST_PORT_NUMBER + "/players";
            driver.get(address);
            driver.manage().window().maximize();
            SeleniumExtension.waitForPageLoadForSeconds(5, driver);
            SeleniumExtension.waitForElementToBeVisible(By.id("info-button"), driver);

            // Click the info modal button (question circle icon in the sidebar footer)
            WebElement infoButton = driver.findElement(By.id("info-button"));
            infoButton.click();

            // Wait for the modal to appear and find the contributors section
            SeleniumExtension.waitForElementToBeVisible(By.id("contributors"), driver, 10);

            // Take screenshot of the contributors section
            WebElement contributorsSection = driver.findElement(By.id("contributors"));
            driver.executeScript("arguments[0].scrollIntoView(true);", contributorsSection);
            Thread.sleep(2000);
            File screenshot = contributorsSection.getScreenshotAs(OutputType.FILE);

            // Save to project root
            Path outputPath = Path.of("Contributors.png");
            Files.move(screenshot.toPath(), outputPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved to: " + outputPath.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
            e.printStackTrace();
            // Still take a full page screenshot for debugging
            try {
                File fullScreenshot = driver.getScreenshotAs(OutputType.FILE);
                Path debugPath = Path.of("contributors_screenshot_debug.png");
                Files.move(fullScreenshot.toPath(), debugPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Debug screenshot saved to: " + debugPath.toAbsolutePath());
            } catch (Exception ex) {
                System.err.println("Failed to save debug screenshot: " + ex.getMessage());
            }
        } finally {
            system.disable();
            driver.quit();
        }
    }

    private static ChromeDriver createChromeDriver() {
        String driverLocation = getChromeDriverLocation();
        if (driverLocation == null) {
            throw new IllegalStateException("Chrome driver location not specified for this OS type");
        }
        if (!new File(driverLocation).exists()) {
            throw new IllegalStateException("Chrome driver not found at " + driverLocation);
        }

        System.setProperty("webdriver.chrome.driver", driverLocation);

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--enable-javascript");
        chromeOptions.addArguments("--ignore-certificate-errors");
        chromeOptions.addArguments("--remote-allow-origins=*");
        chromeOptions.setCapability(ChromeOptions.LOGGING_PREFS, getLoggingPreferences());

        // Using environment variable assumes linux
        if (System.getenv(CIProperties.CHROME_DRIVER) != null) {
            chromeOptions.setBinary("/usr/bin/google-chrome-stable");
            chromeOptions.addArguments("--headless=new");
            chromeOptions.addArguments("--dns-prefetch-disable");
        }

        return new ChromeDriver(chromeOptions);
    }

    private static LoggingPreferences getLoggingPreferences() {
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.INFO);
        logPrefs.enable(LogType.BROWSER, Level.INFO);
        logPrefs.enable(LogType.DRIVER, Level.INFO);
        return logPrefs;
    }

    private static String getChromeDriverLocation() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "C:\\chromedriver.exe";
        }
        return System.getenv(CIProperties.CHROME_DRIVER);
    }

    private static PlanSystem setupSystem() throws IOException {
        PluginMockComponent mockComponent =
                new PluginMockComponent(Files.createTempDirectory("temp-plan-"));
        PlanPluginComponent component = mockComponent.getComponent();

        PlanSystem system = component.system();
        system.getConfigSystem().getConfig()
                .set(WebserverSettings.PORT, TEST_PORT_NUMBER);
        return system;
    }
}
