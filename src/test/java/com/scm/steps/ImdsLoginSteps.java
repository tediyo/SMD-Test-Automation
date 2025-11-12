package com.scm.steps;

import com.scm.utils.DriverManager;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ImdsLoginSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public ImdsLoginSteps() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Given("I navigate to the IMDS login page")
    public void i_navigate_to_the_imds_login_page() {
        // Check system property first, then environment variable, then use default
        String imdsUrl = System.getProperty("imds.url");
        if (imdsUrl == null || imdsUrl.isEmpty()) {
            imdsUrl = System.getenv("IMDS_URL");
        }
        if (imdsUrl == null || imdsUrl.isEmpty()) {
            // Default IMDS URL
            imdsUrl = "https://imds.azureapps.cdl.af.mil/imds/fs/fs000cams.html";
        }
        
        // Add delay before navigation to avoid rate limiting
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        
        // Navigate directly to the URL first (this will trigger certificate warning)
        System.out.println("Navigating to: " + imdsUrl);
        driver.get(imdsUrl);
        
        // Wait a bit for page/certificate warning to appear
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        
        // Handle certificate security warning - try multiple times with longer waits
        boolean certificateHandled = false;
        for (int attempt = 0; attempt < 5 && !certificateHandled; attempt++) {
            try {
                // Try to find and click the details button
                WebDriverWait certWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                WebElement detailsButton = certWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id='details-button']")));
                
                if (detailsButton != null && detailsButton.isDisplayed()) {
                    System.out.println("Found certificate details button, clicking...");
                    detailsButton.click();
                    Thread.sleep(2000);
                    
                    // Now try to find and click the proceed link
                    try {
                        WebElement proceedLink = certWait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[@id='proceed-link']")));
                        
                        if (proceedLink != null && proceedLink.isDisplayed()) {
                            System.out.println("Found proceed link, clicking...");
                            proceedLink.click();
                            Thread.sleep(3000);
                            certificateHandled = true;
                            System.out.println("Certificate warning handled successfully");
                        }
                    } catch (Exception e2) {
                        System.out.println("Proceed link not found on attempt " + (attempt + 1) + ", retrying...");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } catch (Exception e) {
                // Details button not found, might already be past certificate warning
                System.out.println("Certificate details button not found on attempt " + (attempt + 1) + 
                    " (this might be OK if already past certificate warning): " + e.getMessage());
                if (attempt < 4) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    // If we've tried multiple times and no certificate warning, assume we're past it
                    certificateHandled = true;
                }
            }
        }
        
        // Check for rate limiting
        String pageSource = driver.getPageSource();
        if (pageSource.contains("rate_limited") || pageSource.contains("\"rate_limited\":true")) {
            throw new RuntimeException("Rate limited. Please wait before trying again.");
        }
        
        // Wait for page to fully load (wait for JavaScript to execute)
        WebDriverWait pageLoadWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        try {
            // Wait for document.readyState to be complete
            pageLoadWait.until(webDriver -> {
                JavascriptExecutor js = (JavascriptExecutor) webDriver;
                return "complete".equals(js.executeScript("return document.readyState"));
            });
            
            // Additional wait for any dynamic content to load
            Thread.sleep(5000);
        } catch (Exception e) {
            System.out.println("Warning: Page load wait completed with exception: " + e.getMessage());
            // Continue anyway
        }
        
        String pageTitle = driver.getTitle();
        pageSource = driver.getPageSource(); // Refresh page source after certificate handling
        
        // Check for error messages in the page
        if (pageTitle != null && pageTitle.contains("Error")) {
            System.out.println("Warning: Page title indicates an error: " + pageTitle);
        }
        
        // Check for specific error messages - but wait a bit more as JavaScript might fix it
        boolean hasError = pageSource.contains("Transaction Failed") || 
                          pageSource.contains("The requested resource does not exist") ||
                          (pageSource.contains("does not exist") && pageSource.contains("IMDS CDB"));
        
        if (hasError) {
            // Wait a bit more and check again - sometimes JavaScript loads content after initial render
            System.out.println("Error detected, waiting additional time for JavaScript to load content...");
            try {
                Thread.sleep(10000); // Wait 10 more seconds
                pageSource = driver.getPageSource();
                pageTitle = driver.getTitle();
                hasError = pageSource.contains("Transaction Failed") || 
                          pageSource.contains("The requested resource does not exist") ||
                          (pageSource.contains("does not exist") && pageSource.contains("IMDS CDB"));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            
            if (hasError) {
                // Print current URL to help debug
                String currentUrl = driver.getCurrentUrl();
                System.out.println("Current URL after navigation: " + currentUrl);
                System.out.println("Expected URL: " + imdsUrl);
                
                throw new RuntimeException(
                    "IMDS returned an error page. The URL '" + imdsUrl + "' appears to be invalid or the resource does not exist.\n" +
                    "Current URL: " + currentUrl + "\n" +
                    "Page title: " + pageTitle + "\n" +
                    "Error message: 'Transaction Failed - The requested resource does not exist.'\n" +
                    "\nThis might be due to:\n" +
                    "1. The page detecting automation and blocking access\n" +
                    "2. The URL being incorrect or needing a different entry point\n" +
                    "3. The page requiring authentication, cookies, or specific headers\n" +
                    "4. The page needing to be accessed through a parent frame\n" +
                    "\nTroubleshooting:\n" +
                    "- Please verify the exact URL that works in manual Chrome\n" +
                    "- Check if you need to access it through a different URL first\n" +
                    "- Verify if cookies or session data are required\n" +
                    "- The page source shows 'fs = window.parent;' suggesting it expects to be in a frame"
                );
            }
        }
        
        // Check for iframes that might contain the login form
        // We need to find the iframe that contains the TerminalId field
        boolean switchedToFrame = false;
        try {
            java.util.List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
            System.out.println("Found " + iframes.size() + " iframe(s) on the page");
            
            // Try each iframe to find the one with the login form
            for (int i = 0; i < iframes.size(); i++) {
                try {
                    WebElement iframe = iframes.get(i);
                    String iframeSrc = iframe.getAttribute("src");
                    System.out.println("Checking iframe " + i + " with src: " + iframeSrc);
                    
                    // Skip empty or tracking iframes
                    if (iframeSrc == null || iframeSrc.isEmpty() || 
                        iframeSrc.equals("javascript:void(0)") ||
                        iframeSrc.contains("boomerang") ||
                        iframeSrc.contains("matomo")) {
                        continue;
                    }
                    
                    // Switch to this iframe
                    driver.switchTo().defaultContent(); // Make sure we're at root first
                    driver.switchTo().frame(i);
                    System.out.println("Switched to iframe " + i);
                    
                    // Wait for iframe content to load
                    Thread.sleep(3000);
                    
                    // Check if this iframe contains the TerminalId field
                    try {
                        WebDriverWait iframeWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                        WebElement terminalIdCheck = iframeWait.until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//*[@id='TerminalId']")));
                        if (terminalIdCheck != null) {
                            System.out.println("Found TerminalId field in iframe " + i + " - this is the correct iframe!");
                            switchedToFrame = true;
                            break;
                        }
                    } catch (Exception e) {
                        // TerminalId not found in this iframe, try next one
                        System.out.println("TerminalId not found in iframe " + i + ", trying next iframe...");
                        driver.switchTo().defaultContent(); // Go back to root before trying next iframe
                        continue;
                    }
                } catch (Exception e) {
                    System.out.println("Error checking iframe " + i + ": " + e.getMessage());
                    driver.switchTo().defaultContent(); // Make sure we're back at root
                    continue;
                }
            }
            
            // If we didn't find TerminalId in any iframe, check the main content
            if (!switchedToFrame) {
                driver.switchTo().defaultContent();
                System.out.println("TerminalId not found in any iframe, checking main content");
                try {
                    Thread.sleep(2000);
                    WebElement terminalIdCheck = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[@id='TerminalId']")));
                    if (terminalIdCheck != null) {
                        System.out.println("Found TerminalId in main content");
                        switchedToFrame = true;
                    }
                } catch (Exception e) {
                    System.out.println("TerminalId not found in main content either");
                }
            }
        } catch (Exception e) {
            System.out.println("Error handling iframes: " + e.getMessage());
            driver.switchTo().defaultContent(); // Make sure we're at root
        }
        
        if (!switchedToFrame) {
            System.out.println("Warning: Could not find TerminalId field. Staying in current context.");
        }
        
        // Additional wait for page to fully load
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @When("I enter terminal ID {string}")
    public void i_enter_terminal_id(String terminalId) {
        // Wait a bit more for page to fully load
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        // Find the terminal ID input field using the correct selector
        WebElement terminalIdField;
        try {
            // Try the correct selector first: //*[@id="TerminalId"]
            terminalIdField = extendedWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id='TerminalId']")));
            System.out.println("Found TerminalId field using xpath //*[@id='TerminalId']");
        } catch (Exception e) {
            // Fallback: try by ID directly
            try {
                terminalIdField = extendedWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("TerminalId")));
                System.out.println("Found TerminalId field using id='TerminalId'");
            } catch (Exception e2) {
                // If still not found, print debug info and throw error
                System.out.println("Current URL: " + driver.getCurrentUrl());
                System.out.println("Page title: " + driver.getTitle());
                
                // Check what input elements exist
                java.util.List<WebElement> allInputs = driver.findElements(By.tagName("input"));
                System.out.println("Total input elements found: " + allInputs.size());
                for (int i = 0; i < allInputs.size() && i < 10; i++) {
                    try {
                        WebElement input = allInputs.get(i);
                        System.out.println("Input " + i + ": type=" + input.getAttribute("type") + 
                            ", id=" + input.getAttribute("id") + 
                            ", name=" + input.getAttribute("name") + 
                            ", visible=" + input.isDisplayed());
                    } catch (Exception ex) {
                        System.out.println("Input " + i + ": error reading attributes");
                    }
                }
                
                throw new RuntimeException("Could not find TerminalId input field. Tried: //*[@id='TerminalId'] and id='TerminalId'", e2);
            }
        }
        
        // Scroll element into view
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", terminalIdField);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        
        terminalIdField.clear();
        terminalIdField.sendKeys(terminalId);
        System.out.println("Entered terminal ID: " + terminalId);
    }

    @When("I click the IMDS login button")
    public void i_click_the_imds_login_button() {
        // Find the login button using the correct selector: //*[@id="TerminalLogon"]
        WebElement loginButton;
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        try {
            // Try the correct selector first: //*[@id="TerminalLogon"]
            loginButton = extendedWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='TerminalLogon']")));
            System.out.println("Found TerminalLogon button using xpath //*[@id='TerminalLogon']");
        } catch (Exception e) {
            // Fallback: try by ID directly
            try {
                loginButton = extendedWait.until(ExpectedConditions.elementToBeClickable(
                    By.id("TerminalLogon")));
                System.out.println("Found TerminalLogon button using id='TerminalLogon'");
            } catch (Exception e2) {
                throw new RuntimeException("Could not find TerminalLogon button. Tried: //*[@id='TerminalLogon'] and id='TerminalLogon'", e2);
            }
        }
        
        // Scroll element into view before clicking
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loginButton);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        
        loginButton.click();
        System.out.println("Clicked TerminalLogon button");
    }

    @Then("I should be logged into IMDS successfully")
    public void i_should_be_logged_into_imds_successfully() {
        // Wait for navigation after login
        // TODO: Update to check for actual success indicator (e.g., URL change, success message)
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        try {
            // Wait for URL to change from login page
            extendedWait.until(ExpectedConditions.not(
                ExpectedConditions.urlContains("login")));
        } catch (Exception e) {
            // Alternative: Wait for a success message or dashboard element
            // TODO: Update selector to match actual success indicator
            extendedWait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("dashboard")));
        }
    }

    @Then("I should see the IMDS dashboard or home page")
    public void i_should_see_the_imds_dashboard_or_home_page() {
        // TODO: Update selector to match actual dashboard/home page element
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        // Check for dashboard or home page indicators
        boolean dashboardVisible = false;
        
        try {
            // Try multiple possible selectors for dashboard/home page
            extendedWait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.id("dashboard")),
                ExpectedConditions.presenceOfElementLocated(By.className("dashboard")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='dashboard']")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[contains(text(), 'Dashboard')]")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[contains(text(), 'Home')]"))
            ));
            dashboardVisible = true;
        } catch (Exception e) {
            // If specific elements not found, check if URL indicates dashboard
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("login")) {
                dashboardVisible = true;
            }
        }
        
        Assert.assertTrue("IMDS dashboard or home page should be visible after login", 
                dashboardVisible);
    }
 @Then("I log off from IMDS")
public void i_log_off_from_imds() {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

    try {
        driver.switchTo().defaultContent();
        List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
        System.out.println("Found " + iframes.size() + " iframes on page.");

        boolean found = false;
        for (int i = 0; i < iframes.size(); i++) {
            driver.switchTo().defaultContent();
            driver.switchTo().frame(i);
            try {
                WebElement logoffButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[@id='div1']/table[1]/tbody/tr[2]/td[5]/input")
                ));
                if (logoffButton.isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", logoffButton);
                    Thread.sleep(500);
                    logoffButton.click();
                    System.out.println("✅ Clicked Logoff button inside iframe index " + i);
                    found = true;
                    break;
                }
            } catch (Exception ignored) {}
        }

        if (!found) {
            // Try in main content
            driver.switchTo().defaultContent();
            WebElement logoffButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='div1']/table[1]/tbody/tr[2]/td[5]/input")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", logoffButton);
            logoffButton.click();
            System.out.println("✅ Clicked Logoff button in main content (fallback).");
        }

    } catch (Exception e) {
        System.out.println("❌ Could not find or click Logoff button: " + e.getMessage());
        throw e;
    }
}
}