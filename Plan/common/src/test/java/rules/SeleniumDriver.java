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
package rules;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.rules.ExternalResource;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.ArrayList;

public class SeleniumDriver extends ExternalResource {

    private WebDriver driver;

    @Override
    protected void before() {
        String driverLocation = getChromeDriverLocation();
        Assume.assumeNotNull("rules.SeleniumDriver: Chrome driver location not specified for this OS type", driverLocation);
        Assume.assumeTrue("rules.SeleniumDriver: Chrome driver not found at " + driverLocation, new File(driverLocation).exists());

        driver = getChromeWebDriver();
    }
    
    private WebDriver getChromeWebDriver() {
        if (System.getProperty("TRAVIS").equals("true")) {
            final ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setBinary("/usr/bin/google-chrome-stable");
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--disable-gpu");

            final DesiredCapabilities dc = new DesiredCapabilities();
            dc.setJavascriptEnabled(true);
            dc.setCapability(
                ChromeOptions.CAPABILITY, chromeOptions
            );

            return new ChromeDriver(dc);
        } else {
            return new ChromeDriver();
        }
    }

    private String getChromeDriverLocation() {
        if (SystemUtils.IS_OS_WINDOWS) {
            String driverLocation = "C:\\chromedriver.exe";
            System.setProperty("webdriver.chrome.driver", driverLocation);
            return driverLocation;
        }
        return System.getProperty("webdriver.chrome.driver");
    }

    public void newTab() {
        WebElement body = driver.findElement(By.tagName("body"));
        body.sendKeys(Keys.CONTROL + "t");
        driver.switchTo().window(new ArrayList<>(driver.getWindowHandles()).get(0));
    }

    public WebDriver getDriver() {
        return driver;
    }

    @Override
    protected void after() {
        if (driver != null) {
            driver.quit();
        }
    }
}
