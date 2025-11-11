package com.scm.steps;

import com.scm.utils.DriverManager;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleSearchSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public GoogleSearchSteps() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Given("I am on the Google homepage")
    public void i_am_on_the_google_homepage() {
        driver.get("https://www.google.com");
        // Handle cookie consent if present
        try {
            WebElement acceptButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("L2AGLb")));
            acceptButton.click();
        } catch (Exception e) {
            // Cookie consent might not be present, continue
        }
    }

    @When("I search for {string}")
    public void i_search_for(String searchTerm) {
        WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("q")));
        searchBox.clear();
        searchBox.sendKeys(searchTerm);
        searchBox.submit();
    }

    @Then("I should see search results containing {string}")
    public void i_should_see_search_results_containing(String expectedText) {
        // Wait for the search results page to load completely
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        try {
            // Wait for any indication that results have loaded
            extendedWait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.id("search")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-async-context]")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.g")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3"))
            ));
            
            // Additional wait to ensure results are rendered
            Thread.sleep(2000);
        } catch (Exception e) {
            // Continue even if wait fails - Sample try to find results
        }
        
        // Try multiple selector strategies for Google results
        List<WebElement> results = driver.findElements(By.cssSelector("h3"));
        
        // If no h3 found, try alternative selectors
        if (results.isEmpty()) {
            results = driver.findElements(By.cssSelector("div.g h3, div[data-hveid] h3, a h3"));
        }
        
        // Get all visible h3 elements with text
        List<String> resultTexts = results.stream()
                .filter(element -> {
                    try {
                        return element.isDisplayed() && !element.getText().trim().isEmpty();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(WebElement::getText)
                .collect(Collectors.toList());
        
        // Check if any result contains the expected text
        boolean found = resultTexts.stream()
                .anyMatch(text -> text.toLowerCase().contains(expectedText.toLowerCase()));
        
        String displayResults = resultTexts.stream()
                .limit(5) // Only show first 5 for readability
                .collect(Collectors.joining(" | "));
        
        Assert.assertTrue("Search results should contain: " + expectedText + 
                ". Found " + resultTexts.size() + " results: " + displayResults, found);
    }

    @Then("the page title should contain {string}")
    public void the_page_title_should_contain(String expectedTitle) {
        wait.until(ExpectedConditions.titleContains(expectedTitle));
        String actualTitle = driver.getTitle();
        Assert.assertTrue("Page title should contain: " + expectedTitle, 
                actualTitle.toLowerCase().contains(expectedTitle.toLowerCase()));
    }
}
