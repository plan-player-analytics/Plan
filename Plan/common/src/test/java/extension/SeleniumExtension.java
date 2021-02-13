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
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import utilities.CIProperties;

import java.io.File;
import java.util.ArrayList;

import static org.openqa.selenium.remote.CapabilityType.SUPPORTS_JAVASCRIPT;

/**
 * Selenium JUnit 5 Extension.
 *
 * @author AuroraLS3
 */
public class SeleniumExtension implements ParameterResolver, BeforeAllCallback, AfterAllCallback {

    private WebDriver driver;

    public static void newTab(WebDriver driver) {
        WebElement body = driver.findElement(By.tagName("body"));
        body.sendKeys(Keys.CONTROL + "t");
        driver.switchTo().window(new ArrayList<>(driver.getWindowHandles()).get(0));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(WebDriver.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
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

    private WebDriver getChromeWebDriver() {
        if (System.getenv(CIProperties.CHROME_DRIVER) != null) {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setBinary("/usr/bin/google-chrome-stable");
            chromeOptions.setHeadless(true);
            chromeOptions.setCapability(SUPPORTS_JAVASCRIPT, true);

            return new ChromeDriver(chromeOptions);
        } else {
            return new ChromeDriver();
        }
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
