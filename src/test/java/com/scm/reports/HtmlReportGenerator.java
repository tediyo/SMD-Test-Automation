package com.scm.reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates interactive HTML reports from Cucumber JSON output
 * Supports multiple report formats: Dashboard, Detailed, and Timeline
 */
public class HtmlReportGenerator {
    private static final String REPORTS_DIR = "target/html-reports";
    private final Gson gson = new Gson();

    /**
     * Generate all report formats from Cucumber JSON
     * @param jsonFilePath Path to Cucumber JSON report
     */
    public void generateReports(String jsonFilePath) {
        try {
            // Create reports directory
            new File(REPORTS_DIR).mkdirs();

            // Read and parse JSON
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            JsonArray features = gson.fromJson(jsonContent, JsonArray.class);

            // Generate different report formats
            generateDashboardReport(features);
            generateDetailedReport(features);
            generateTimelineReport(features);
            generateIndexPage();

            System.out.println("HTML reports generated successfully in: " + REPORTS_DIR);
        } catch (Exception e) {
            System.err.println("Error generating reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate Dashboard-style report with summary statistics
     */
    private void generateDashboardReport(JsonArray features) throws IOException {
        ReportData data = parseReportData(features);
        String html = generateDashboardHtml(data);
        writeFile(REPORTS_DIR + "/dashboard.html", html);
    }

    /**
     * Generate Detailed report with full test information
     */
    private void generateDetailedReport(JsonArray features) throws IOException {
        ReportData data = parseReportData(features);
        String html = generateDetailedHtml(data);
        writeFile(REPORTS_DIR + "/detailed.html", html);
    }

    /**
     * Generate Timeline report showing test execution over time
     */
    private void generateTimelineReport(JsonArray features) throws IOException {
        ReportData data = parseReportData(features);
        String html = generateTimelineHtml(data);
        writeFile(REPORTS_DIR + "/timeline.html", html);
    }

    /**
     * Generate index page with links to all reports
     */
    private void generateIndexPage() throws IOException {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Test Reports - Index</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        min-height: 100vh;
                        padding: 40px 20px;
                    }
                    .container {
                        max-width: 1200px;
                        margin: 0 auto;
                    }
                    h1 {
                        color: white;
                        text-align: center;
                        margin-bottom: 50px;
                        font-size: 2.5em;
                        text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
                    }
                    .report-cards {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                        gap: 30px;
                    }
                    .card {
                        background: white;
                        border-radius: 15px;
                        padding: 30px;
                        box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                        transition: transform 0.3s, box-shadow 0.3s;
                        text-decoration: none;
                        color: inherit;
                        display: block;
                    }
                    .card:hover {
                        transform: translateY(-5px);
                        box-shadow: 0 15px 40px rgba(0,0,0,0.4);
                    }
                    .card h2 {
                        color: #667eea;
                        margin-bottom: 15px;
                        font-size: 1.8em;
                    }
                    .card p {
                        color: #666;
                        line-height: 1.6;
                        margin-bottom: 20px;
                    }
                    .card-icon {
                        font-size: 3em;
                        margin-bottom: 15px;
                    }
                    .badge {
                        display: inline-block;
                        padding: 5px 15px;
                        border-radius: 20px;
                        font-size: 0.9em;
                        font-weight: bold;
                        margin-top: 10px;
                    }
                    .badge-dashboard { background: #e3f2fd; color: #1976d2; }
                    .badge-detailed { background: #f3e5f5; color: #7b1fa2; }
                    .badge-timeline { background: #e8f5e9; color: #388e3c; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>📊 Test Execution Reports</h1>
                    <div class="report-cards">
                        <a href="dashboard.html" class="card">
                            <div class="card-icon">📈</div>
                            <h2>Dashboard View</h2>
                            <p>High-level overview with summary statistics, pass/fail rates, and key metrics at a glance.</p>
                            <span class="badge badge-dashboard">Summary & Stats</span>
                        </a>
                        <a href="detailed.html" class="card">
                            <div class="card-icon">📋</div>
                            <h2>Detailed View</h2>
                            <p>Comprehensive test details including step-by-step execution, response times, and error messages.</p>
                            <span class="badge badge-detailed">Full Details</span>
                        </a>
                        <a href="timeline.html" class="card">
                            <div class="card-icon">⏱️</div>
                            <h2>Timeline View</h2>
                            <p>Visual timeline showing test execution order, duration, and response time trends.</p>
                            <span class="badge badge-timeline">Time Analysis</span>
                        </a>
                    </div>
                </div>
            </body>
            </html>
            """;
        writeFile(REPORTS_DIR + "/index.html", html);
    }

    /**
     * Parse Cucumber JSON into structured data
     */
    private ReportData parseReportData(JsonArray features) {
        ReportData data = new ReportData();
        data.generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        for (JsonElement featureElement : features) {
            JsonObject feature = featureElement.getAsJsonObject();
            String featureName = feature.get("name").getAsString();

            if (feature.has("elements")) {
                JsonArray elements = feature.getAsJsonArray("elements");
                for (JsonElement elementElement : elements) {
                    JsonObject element = elementElement.getAsJsonObject();
                    if ("scenario".equals(element.get("type").getAsString())) {
                        TestScenario scenario = new TestScenario();
                        scenario.featureName = featureName;
                        scenario.name = element.get("name").getAsString();
                        scenario.tags = new ArrayList<>();
                        
                        if (element.has("tags")) {
                            for (JsonElement tag : element.getAsJsonArray("tags")) {
                                scenario.tags.add(tag.getAsJsonObject().get("name").getAsString());
                            }
                        }

                        // Parse steps
                        if (element.has("steps")) {
                            JsonArray steps = element.getAsJsonArray("steps");
                            long totalDuration = 0;
                            for (JsonElement stepElement : steps) {
                                JsonObject step = stepElement.getAsJsonObject();
                                TestStep testStep = new TestStep();
                                testStep.name = step.get("name").getAsString();
                                testStep.keyword = step.has("keyword") ? step.get("keyword").getAsString() : "";
                                
                                // Get duration
                                if (step.has("result") && step.getAsJsonObject("result").has("duration")) {
                                    long duration = step.getAsJsonObject("result").get("duration").getAsLong() / 1_000_000; // Convert to milliseconds
                                    testStep.duration = duration;
                                    totalDuration += duration;
                                }
                                
                                // Get status
                                if (step.has("result")) {
                                    String status = step.getAsJsonObject("result").get("status").getAsString();
                                    testStep.status = status;
                                    if ("failed".equals(status)) {
                                        scenario.status = "failed";
                                        if (step.getAsJsonObject("result").has("error_message")) {
                                            testStep.errorMessage = step.getAsJsonObject("result").get("error_message").getAsString();
                                        }
                                    }
                                }
                                
                                scenario.steps.add(testStep);
                            }
                            scenario.duration = totalDuration;
                        }

                        if (scenario.status == null) {
                            scenario.status = "passed";
                        }

                        data.scenarios.add(scenario);
                        data.totalTests++;
                        if ("passed".equals(scenario.status)) {
                            data.passedTests++;
                        } else if ("failed".equals(scenario.status)) {
                            data.failedTests++;
                        }
                        data.totalDuration += scenario.duration;
                    }
                }
            }
        }

        return data;
    }

    /**
     * Generate Dashboard HTML
     */
    private String generateDashboardHtml(ReportData data) {
        double passRate = data.totalTests > 0 ? (data.passedTests * 100.0 / data.totalTests) : 0;
        double avgDuration = data.totalTests > 0 ? (data.totalDuration / (double) data.totalTests) : 0;

        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Test Dashboard</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: #f5f7fa;
                        padding: 20px;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px;
                        border-radius: 10px;
                        margin-bottom: 30px;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .header h1 { margin-bottom: 10px; }
                    .header p { opacity: 0.9; }
                    .nav {
                        margin-bottom: 20px;
                    }
                    .nav a {
                        display: inline-block;
                        padding: 10px 20px;
                        margin-right: 10px;
                        background: white;
                        color: #667eea;
                        text-decoration: none;
                        border-radius: 5px;
                        font-weight: bold;
                    }
                    .nav a:hover { background: #667eea; color: white; }
                    .stats-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                        gap: 20px;
                        margin-bottom: 30px;
                    }
                    .stat-card {
                        background: white;
                        padding: 25px;
                        border-radius: 10px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .stat-card h3 {
                        color: #666;
                        font-size: 0.9em;
                        margin-bottom: 10px;
                        text-transform: uppercase;
                    }
                    .stat-card .value {
                        font-size: 2.5em;
                        font-weight: bold;
                        margin-bottom: 5px;
                    }
                    .stat-card.passed .value { color: #4caf50; }
                    .stat-card.failed .value { color: #f44336; }
                    .stat-card.total .value { color: #2196f3; }
                    .stat-card.duration .value { color: #ff9800; }
                    .chart-container {
                        background: white;
                        padding: 25px;
                        border-radius: 10px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        margin-bottom: 30px;
                    }
                    .progress-bar {
                        width: 100%%;
                        height: 40px;
                        background: #e0e0e0;
                        border-radius: 20px;
                        overflow: hidden;
                        margin: 20px 0;
                    }
                    .progress-fill {
                        height: 100%%;
                        background: linear-gradient(90deg, #4caf50, #8bc34a);
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: white;
                        font-weight: bold;
                        transition: width 0.3s;
                    }
                    .test-list {
                        background: white;
                        border-radius: 10px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .test-item {
                        padding: 15px 20px;
                        border-bottom: 1px solid #eee;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }
                    .test-item:last-child { border-bottom: none; }
                    .test-item.failed { background: #ffebee; }
                    .test-name { font-weight: 500; }
                    .test-status {
                        padding: 5px 15px;
                        border-radius: 20px;
                        font-size: 0.85em;
                        font-weight: bold;
                    }
                    .status-passed { background: #c8e6c9; color: #2e7d32; }
                    .status-failed { background: #ffcdd2; color: #c62828; }
                    .test-duration { color: #666; font-size: 0.9em; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>📊 Test Execution Dashboard</h1>
                    <p>Generated on: %s</p>
                </div>
                <div class="nav">
                    <a href="index.html">🏠 Home</a>
                    <a href="dashboard.html">📈 Dashboard</a>
                    <a href="detailed.html">📋 Detailed</a>
                    <a href="timeline.html">⏱️ Timeline</a>
                </div>
                <div class="stats-grid">
                    <div class="stat-card total">
                        <h3>Total Tests</h3>
                        <div class="value">%d</div>
                    </div>
                    <div class="stat-card passed">
                        <h3>Passed</h3>
                        <div class="value">%d</div>
                    </div>
                    <div class="stat-card failed">
                        <h3>Failed</h3>
                        <div class="value">%d</div>
                    </div>
                    <div class="stat-card duration">
                        <h3>Avg Duration</h3>
                        <div class="value">%.2fs</div>
                    </div>
                </div>
                <div class="chart-container">
                    <h2>Pass Rate</h2>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: %.2f%%">%.2f%%</div>
                    </div>
                </div>
                <div class="test-list">
                    <h2 style="padding: 20px; border-bottom: 2px solid #667eea;">Test Results</h2>
                    %s
                </div>
            </body>
            </html>
            """, 
            data.generatedAt,
            data.totalTests,
            data.passedTests,
            data.failedTests,
            avgDuration / 1000.0,
            passRate, passRate,
            generateTestListHtml(data)
        );
    }

    /**
     * Generate Detailed HTML
     */
    private String generateDetailedHtml(ReportData data) {
        StringBuilder scenariosHtml = new StringBuilder();
        for (TestScenario scenario : data.scenarios) {
            StringBuilder stepsHtml = new StringBuilder();
            for (TestStep step : scenario.steps) {
                String statusClass = "status-" + step.status;
                String errorHtml = step.errorMessage != null ? 
                    String.format("<div style='color: #c62828; margin-top: 5px; font-size: 0.9em;'>❌ %s</div>", 
                        escapeHtml(step.errorMessage)) : "";
                
                stepsHtml.append(String.format("""
                    <div class="step-item">
                        <div class="step-header">
                            <span class="step-keyword">%s</span>
                            <span class="step-name">%s</span>
                            <span class="step-status %s">%s</span>
                            <span class="step-duration">%s</span>
                        </div>
                        %s
                    </div>
                    """,
                    escapeHtml(step.keyword),
                    escapeHtml(step.name),
                    statusClass,
                    step.status.toUpperCase(),
                    formatDuration(step.duration),
                    errorHtml
                ));
            }

            String scenarioStatusClass = "status-" + scenario.status;
            scenariosHtml.append(String.format("""
                <div class="scenario-card %s">
                    <div class="scenario-header">
                        <h3>%s</h3>
                        <div class="scenario-meta">
                            <span class="scenario-tags">%s</span>
                            <span class="scenario-status %s">%s</span>
                            <span class="scenario-duration">⏱️ %s</span>
                        </div>
                    </div>
                    <div class="scenario-steps">
                        <h4>Steps:</h4>
                        %s
                    </div>
                </div>
                """,
                scenario.status,
                escapeHtml(scenario.name),
                scenario.tags.isEmpty() ? "" : String.join(", ", scenario.tags),
                scenarioStatusClass,
                scenario.status.toUpperCase(),
                formatDuration(scenario.duration),
                stepsHtml.toString()
            ));
        }

        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Detailed Test Report</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: #f5f7fa;
                        padding: 20px;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px;
                        border-radius: 10px;
                        margin-bottom: 30px;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .nav {
                        margin-bottom: 20px;
                    }
                    .nav a {
                        display: inline-block;
                        padding: 10px 20px;
                        margin-right: 10px;
                        background: white;
                        color: #667eea;
                        text-decoration: none;
                        border-radius: 5px;
                        font-weight: bold;
                    }
                    .nav a:hover { background: #667eea; color: white; }
                    .scenario-card {
                        background: white;
                        border-radius: 10px;
                        margin-bottom: 20px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .scenario-card.failed { border-left: 5px solid #f44336; }
                    .scenario-card.passed { border-left: 5px solid #4caf50; }
                    .scenario-header {
                        padding: 20px;
                        background: #f8f9fa;
                        border-bottom: 1px solid #eee;
                    }
                    .scenario-header h3 {
                        color: #333;
                        margin-bottom: 10px;
                    }
                    .scenario-meta {
                        display: flex;
                        gap: 15px;
                        align-items: center;
                        flex-wrap: wrap;
                    }
                    .scenario-status {
                        padding: 5px 15px;
                        border-radius: 20px;
                        font-size: 0.85em;
                        font-weight: bold;
                    }
                    .status-passed { background: #c8e6c9; color: #2e7d32; }
                    .status-failed { background: #ffcdd2; color: #c62828; }
                    .scenario-duration {
                        color: #666;
                        font-size: 0.9em;
                    }
                    .scenario-tags {
                        color: #667eea;
                        font-size: 0.85em;
                    }
                    .scenario-steps {
                        padding: 20px;
                    }
                    .scenario-steps h4 {
                        margin-bottom: 15px;
                        color: #666;
                    }
                    .step-item {
                        padding: 15px;
                        margin-bottom: 10px;
                        background: #f8f9fa;
                        border-radius: 5px;
                        border-left: 3px solid #ddd;
                    }
                    .step-header {
                        display: flex;
                        gap: 10px;
                        align-items: center;
                        flex-wrap: wrap;
                    }
                    .step-keyword {
                        font-weight: bold;
                        color: #667eea;
                    }
                    .step-name {
                        flex: 1;
                        color: #333;
                    }
                    .step-status {
                        padding: 3px 10px;
                        border-radius: 15px;
                        font-size: 0.8em;
                        font-weight: bold;
                    }
                    .step-duration {
                        color: #666;
                        font-size: 0.85em;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>📋 Detailed Test Report</h1>
                    <p>Generated on: %s | Total Tests: %d | Passed: %d | Failed: %d</p>
                </div>
                <div class="nav">
                    <a href="index.html">🏠 Home</a>
                    <a href="dashboard.html">📈 Dashboard</a>
                    <a href="detailed.html">📋 Detailed</a>
                    <a href="timeline.html">⏱️ Timeline</a>
                </div>
                %s
            </body>
            </html>
            """,
            data.generatedAt,
            data.totalTests,
            data.passedTests,
            data.failedTests,
            scenariosHtml.toString()
        );
    }

    /**
     * Generate Timeline HTML
     */
    private String generateTimelineHtml(ReportData data) {
        StringBuilder timelineHtml = new StringBuilder();
        for (TestScenario scenario : data.scenarios) {
            String statusClass = "timeline-item-" + scenario.status;
            timelineHtml.append(String.format("""
                <div class="timeline-item %s">
                    <div class="timeline-marker"></div>
                    <div class="timeline-content">
                        <div class="timeline-header">
                            <h3>%s</h3>
                            <span class="timeline-status status-%s">%s</span>
                        </div>
                        <div class="timeline-meta">
                            <span>⏱️ %s</span>
                            <span>📁 %s</span>
                        </div>
                        <div class="timeline-bar">
                            <div class="timeline-bar-fill" style="width: %.2f%%"></div>
                        </div>
                    </div>
                </div>
                """,
                statusClass,
                escapeHtml(scenario.name),
                scenario.status,
                scenario.status.toUpperCase(),
                formatDuration(scenario.duration),
                escapeHtml(scenario.featureName),
                (scenario.duration / (double) Math.max(data.totalDuration, 1)) * 100
            ));
        }

        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Test Timeline</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: #f5f7fa;
                        padding: 20px;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px;
                        border-radius: 10px;
                        margin-bottom: 30px;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .nav {
                        margin-bottom: 20px;
                    }
                    .nav a {
                        display: inline-block;
                        padding: 10px 20px;
                        margin-right: 10px;
                        background: white;
                        color: #667eea;
                        text-decoration: none;
                        border-radius: 5px;
                        font-weight: bold;
                    }
                    .nav a:hover { background: #667eea; color: white; }
                    .timeline {
                        position: relative;
                        padding: 20px 0;
                    }
                    .timeline::before {
                        content: '';
                        position: absolute;
                        left: 30px;
                        top: 0;
                        bottom: 0;
                        width: 2px;
                        background: #ddd;
                    }
                    .timeline-item {
                        position: relative;
                        padding-left: 80px;
                        margin-bottom: 30px;
                    }
                    .timeline-marker {
                        position: absolute;
                        left: 20px;
                        top: 10px;
                        width: 20px;
                        height: 20px;
                        border-radius: 50%%;
                        border: 3px solid white;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                    }
                    .timeline-item-passed .timeline-marker { background: #4caf50; }
                    .timeline-item-failed .timeline-marker { background: #f44336; }
                    .timeline-content {
                        background: white;
                        padding: 20px;
                        border-radius: 10px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .timeline-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 10px;
                    }
                    .timeline-header h3 {
                        color: #333;
                    }
                    .timeline-status {
                        padding: 5px 15px;
                        border-radius: 20px;
                        font-size: 0.85em;
                        font-weight: bold;
                    }
                    .timeline-meta {
                        display: flex;
                        gap: 20px;
                        color: #666;
                        font-size: 0.9em;
                        margin-bottom: 10px;
                    }
                    .timeline-bar {
                        width: 100%%;
                        height: 8px;
                        background: #e0e0e0;
                        border-radius: 4px;
                        overflow: hidden;
                    }
                    .timeline-bar-fill {
                        height: 100%%;
                        background: linear-gradient(90deg, #667eea, #764ba2);
                        transition: width 0.3s;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>⏱️ Test Execution Timeline</h1>
                    <p>Generated on: %s | Total Duration: %s</p>
                </div>
                <div class="nav">
                    <a href="index.html">🏠 Home</a>
                    <a href="dashboard.html">📈 Dashboard</a>
                    <a href="detailed.html">📋 Detailed</a>
                    <a href="timeline.html">⏱️ Timeline</a>
                </div>
                <div class="timeline">
                    %s
                </div>
            </body>
            </html>
            """,
            data.generatedAt,
            formatDuration(data.totalDuration),
            timelineHtml.toString()
        );
    }

    private String generateTestListHtml(ReportData data) {
        StringBuilder html = new StringBuilder();
        for (TestScenario scenario : data.scenarios) {
            String statusClass = "status-" + scenario.status;
            html.append(String.format("""
                <div class="test-item %s">
                    <div>
                        <div class="test-name">%s</div>
                        <div class="test-duration">⏱️ %s</div>
                    </div>
                    <div>
                        <span class="test-status %s">%s</span>
                    </div>
                </div>
                """,
                scenario.status,
                escapeHtml(scenario.name),
                formatDuration(scenario.duration),
                statusClass,
                scenario.status.toUpperCase()
            ));
        }
        return html.toString();
    }

    private String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else {
            double seconds = milliseconds / 1000.0;
            return String.format("%.2fs", seconds);
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private void writeFile(String filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }

    // Data classes
    private static class ReportData {
        String generatedAt;
        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        long totalDuration = 0;
        List<TestScenario> scenarios = new ArrayList<>();
    }

    private static class TestScenario {
        String featureName;
        String name;
        String status = "passed";
        long duration = 0;
        List<String> tags = new ArrayList<>();
        List<TestStep> steps = new ArrayList<>();
    }

    private static class TestStep {
        String keyword;
        String name;
        String status = "passed";
        long duration = 0;
        String errorMessage;
    }
}

