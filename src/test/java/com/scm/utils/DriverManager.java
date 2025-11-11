package com.scm.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public class DriverManager {
    private static WebDriver driver;
    private static String browserName = System.getProperty("browser", "chrome").toLowerCase();
    private static boolean attachToDebugging = Boolean.parseBoolean(System.getProperty("attachDebug", "false"));
    private static String debugPort = System.getProperty("debugPort", "9222"); // default debugging port

    public static WebDriver getDriver() {
        if (driver == null) {
            driver = createDriver();
        }
        return driver;
    }

    private static WebDriver createDriver() {
        WebDriver webDriver;

        switch (browserName) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();

                if (attachToDebugging) {
                    // Attach to an already running Chrome
                    chromeOptions.setExperimentalOption("debuggerAddress", "localhost:" + debugPort);
                    // No extra automation flags when attaching
                } else {
                    // Fresh Chrome session
                    chromeOptions.addArguments(
                        "user-data-dir=C:/Users/Abel Wondowsen/AppData/Local/Google/Chrome/User Data",
                        "profile-directory=Default",
                        "--disable-blink-features=AutomationControlled",
                        "--ignore-certificate-errors",
                        "--ignore-ssl-errors",
                        "--allow-insecure-localhost",
                        "--start-maximized"
                    );
                    chromeOptions.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
                    chromeOptions.setExperimentalOption("useAutomationExtension", false);
                }

                chromeOptions.setAcceptInsecureCerts(true);

                webDriver = new ChromeDriver(chromeOptions);

                // Remove webdriver flag only for fresh Chrome
                if (!attachToDebugging) {
                    ((JavascriptExecutor) webDriver).executeScript(
                        "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
                    );
                }
                break;

            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments(
                    "user-data-dir=C:/Users/Abel Wondowsen/AppData/Local/Microsoft/Edge/User Data",
                    "profile-directory=Default",
                    "--ignore-certificate-errors",
                    "--ignore-ssl-errors",
                    "--start-maximized"
                );
                edgeOptions.setAcceptInsecureCerts(true);
                webDriver = new EdgeDriver(edgeOptions);
                break;

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setAcceptInsecureCerts(true);
                webDriver = new FirefoxDriver(firefoxOptions);
                break;

            default:
                throw new IllegalArgumentException("Browser not supported: " + browserName);
        }

        webDriver.manage().window().maximize();
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        return webDriver;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public static void closeDriver() {
        if (driver != null) {
            driver.close();
        }
    }
}
