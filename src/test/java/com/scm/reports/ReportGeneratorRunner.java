package com.scm.reports;

/**
 * Standalone runner to generate HTML reports from existing Cucumber JSON output
 * Usage: Run this class after tests complete to generate reports
 */
public class ReportGeneratorRunner {
    public static void main(String[] args) {
        System.out.println("🚀 Starting HTML Report Generation...");
        ReportManager.forceGenerateReports();
        System.out.println("✨ Report generation complete!");
    }
}

