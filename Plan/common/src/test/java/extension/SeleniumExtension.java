package extension;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
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
 * @author Rsl1122
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
        Assume.assumeNotNull("rules.SeleniumDriver: Chrome driver location not specified for this OS type", driverLocation);
        Assume.assumeTrue("rules.SeleniumDriver: Chrome driver not found at " + driverLocation, new File(driverLocation).exists());

        System.setProperty("webdriver.chrome.driver", driverLocation);
        driver = getChromeWebDriver();
    }

    private WebDriver getChromeWebDriver() {
        if (Boolean.parseBoolean(System.getenv(CIProperties.IS_TRAVIS))) {
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
