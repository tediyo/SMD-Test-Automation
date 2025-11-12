package com.scm.steps;

import com.scm.utils.DriverManager;
import com.scm.utils.TestTiming;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.time.LocalDateTime;
import java.util.UUID;

public class Hooks {
    private WebDriver driver;
    private String scenarioId;
    private long scenarioStartTime;

    @Before
    public void setUp(Scenario scenario) {
        scenarioId = UUID.randomUUID().toString();
        scenarioStartTime = System.currentTimeMillis();
        TestTiming.startStep(scenarioId, scenario.getName());
        driver = DriverManager.getDriver();
    }

    @After
    public void tearDown(Scenario scenario) {
        long scenarioDuration = System.currentTimeMillis() - scenarioStartTime;
        TestTiming.endStep(scenarioId);
        
        // Attach timing information to the scenario
        String timingInfo = String.format("Scenario: %s\nDuration: %s\nTimestamp: %s", 
            scenario.getName(), 
            TestTiming.formatDuration(scenarioDuration),
            LocalDateTime.now());
        scenario.attach(timingInfo.getBytes(), "text/plain", "timing_info");
        
        if (scenario.isFailed()) {
            // Take screenshot on failure
            final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", scenario.getName() + "_failure_screenshot");
        }
        DriverManager.quitDriver();
    }
}
