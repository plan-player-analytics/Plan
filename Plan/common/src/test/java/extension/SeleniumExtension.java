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
package extension;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v130.emulation.Emulation;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.awaitility.Awaitility;
import utilities.CIProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Selenium JUnit 5 Extension.
 *
 * @author AuroraLS3
 */
public class SeleniumExtension implements ParameterResolver, BeforeAllCallback, AfterAllCallback {

    private ChromeDriver driver;

    public static void newTab(WebDriver driver) {
        WebElement body = driver.findElement(By.tagName("body"));
        body.sendKeys(Keys.CONTROL + "t");
        driver.switchTo().window(new ArrayList<>(driver.getWindowHandles()).get(0));
    }

    public static void waitForPageLoadForSeconds(int i, ChromeDriver driver) {
        Awaitility.await("waitForPageLoadForSeconds")
                .atMost(i, TimeUnit.SECONDS)
                .until(() -> "complete".equals(driver.executeScript("return document.readyState")));
    }

    public static void waitForElementToBeVisible(By by, ChromeDriver driver) {
        SeleniumExtension.waitForPageLoadForSeconds(5, driver);
        Awaitility.await("waitForElementToBeVisible " + by.toString())
                .atMost(5, TimeUnit.SECONDS)
                .ignoreExceptionsMatching(throwable -> throwable instanceof NoSuchElementException
                        || throwable instanceof StaleElementReferenceException)
                .until(() -> driver.findElement(by).isDisplayed());
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        final Class<?> type = parameterContext.getParameter().getType();
        return WebDriver.class.equals(type) || ChromeDriver.class.equals(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return driver;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        String driverLocation = getChromeDriverLocation();
        Assumptions.assumeFalse(driverLocation == null, "rules.SeleniumDriver: Chrome driver location not specified for this OS type");
        Assumptions.assumeTrue(new File(driverLocation).exists(), "rules.SeleniumDriver: Chrome driver not found at " + driverLocation);

        System.setProperty("webdriver.chrome.driver", driverLocation);
        driver = getChromeWebDriver();
    }

    private ChromeDriver getChromeWebDriver() {
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

    public static void setTimeZone(ChromeDriver chromeDriver, String timeZone) {
        try (DevTools devTools = chromeDriver.getDevTools()) {
            devTools.createSession();
            devTools.send(Emulation.setTimezoneOverride(timeZone));
        }
    }

    private LoggingPreferences getLoggingPreferences() {
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.INFO);
        logPrefs.enable(LogType.PROFILER, Level.INFO);
        logPrefs.enable(LogType.BROWSER, Level.INFO);
        logPrefs.enable(LogType.CLIENT, Level.INFO);
        logPrefs.enable(LogType.DRIVER, Level.INFO);
        return logPrefs;
    }

    private String getChromeDriverLocation() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "C:\\chromedriver.exe";
        }
        return System.getenv(CIProperties.CHROME_DRIVER);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (driver != null) {
            driver.quit();
        }
    }
}
