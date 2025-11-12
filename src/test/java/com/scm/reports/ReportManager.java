package com.scm.reports;

import java.io.File;

/**
 * Manages test report generation
 */
public class ReportManager {
    private static final String CUCUMBER_JSON_PATH = "target/cucumber-reports/cucumber.json";
    private static boolean reportsGenerated = false;

    /**
     * Generate HTML reports from Cucumber JSON output
     * This method is safe to call multiple times - it will only generate once per test run
     */
    public static void generateReports() {
        if (reportsGenerated) {
            return;
        }

        try {
            File jsonFile = new File(CUCUMBER_JSON_PATH);
            if (jsonFile.exists()) {
                HtmlReportGenerator generator = new HtmlReportGenerator();
                generator.generateReports(CUCUMBER_JSON_PATH);
                reportsGenerated = true;
                System.out.println("✅ HTML reports generated successfully!");
                System.out.println("📊 View reports at: target/html-reports/index.html");
            } else {
                System.out.println("⚠️  Cucumber JSON report not found at: " + CUCUMBER_JSON_PATH);
                System.out.println("   Reports will be generated after test execution completes.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error generating reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Force report generation even if already generated
     */
    public static void forceGenerateReports() {
        reportsGenerated = false;
        generateReports();
    }
}

