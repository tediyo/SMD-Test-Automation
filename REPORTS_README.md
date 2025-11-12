# Interactive HTML Test Reports

This project includes an advanced HTML reporting system that generates interactive test reports with multiple viewing formats and detailed response time tracking.

## 📊 Report Formats

The reporting system generates three different HTML report formats:

### 1. **Dashboard View** (`dashboard.html`)
- High-level overview with summary statistics
- Pass/fail rates with visual progress bars
- Key metrics at a glance
- Quick test result summary

### 2. **Detailed View** (`detailed.html`)
- Comprehensive test details
- Step-by-step execution information
- Individual step response times
- Error messages and stack traces
- Full test scenario breakdown

### 3. **Timeline View** (`timeline.html`)
- Visual timeline of test execution
- Test execution order
- Duration visualization
- Response time trends
- Chronological test flow

## 🚀 Usage

### Automatic Report Generation

Reports are automatically generated after test execution completes when running:

```bash
mvn test
```

The reports will be available in: `target/html-reports/`

### Manual Report Generation

To generate reports manually from existing Cucumber JSON output:

```bash
mvn exec:java -Dexec.mainClass="com.scm.reports.ReportGeneratorRunner" -Dexec.classpathScope=test
```

Or run the `ReportGeneratorRunner` class directly from your IDE.

### Viewing Reports

1. Navigate to `target/html-reports/`
2. Open `index.html` in your web browser
3. Choose from Dashboard, Detailed, or Timeline views

## 📈 Features

### Response Time Tracking
- **Step-level timing**: Each test step tracks its execution time
- **Scenario-level timing**: Total execution time per test scenario
- **Average calculations**: Automatic calculation of average response times
- **Formatted display**: Human-readable time formats (ms, seconds)

### Interactive Features
- **Filtering**: Filter tests by status (passed/failed)
- **Sorting**: Sort tests by duration, name, or status
- **Visual indicators**: Color-coded status indicators
- **Progress bars**: Visual representation of pass rates and durations
- **Responsive design**: Works on desktop and mobile devices

### Detailed Information
- **Test steps**: Complete step-by-step breakdown
- **Error messages**: Full error details for failed tests
- **Tags**: Feature tags and test categorization
- **Timestamps**: Test execution timestamps
- **Feature grouping**: Tests organized by feature files

## 📁 Report Structure

```
target/html-reports/
├── index.html          # Main navigation page
├── dashboard.html      # Dashboard view
├── detailed.html       # Detailed view
└── timeline.html       # Timeline view
```

## 🔧 Configuration

### Customizing Report Output Location

Edit `HtmlReportGenerator.java` and modify the `REPORTS_DIR` constant:

```java
private static final String REPORTS_DIR = "target/html-reports";
```

### Customizing Cucumber JSON Location

Edit `ReportManager.java` and modify the `CUCUMBER_JSON_PATH` constant:

```java
private static final String CUCUMBER_JSON_PATH = "target/cucumber-reports/cucumber.json";
```

## 📝 Technical Details

### Dependencies
- **Gson**: JSON parsing for Cucumber JSON reports
- **Java 21**: Required for compilation

### Report Generation Process
1. Tests execute and generate Cucumber JSON output
2. `ReportManager` reads the JSON file
3. `HtmlReportGenerator` parses the data
4. Three HTML report formats are generated
5. Index page is created with navigation links

### Timing Data
- Timing data is collected using the `TestTiming` utility class
- Step-level timing is tracked in test step definitions
- Scenario-level timing is tracked in Hooks
- All timing data is included in the Cucumber JSON output

## 🎨 Report Styling

Reports use modern CSS with:
- Gradient backgrounds
- Card-based layouts
- Responsive grid systems
- Smooth animations and transitions
- Color-coded status indicators

## 🔍 Troubleshooting

### Reports Not Generated
- Ensure tests have completed successfully
- Check that `target/cucumber-reports/cucumber.json` exists
- Verify Maven exec plugin is configured correctly

### Missing Response Times
- Ensure `TestTiming` utility is being used in step definitions
- Check that step methods include timing start/end calls
- Verify timing data is being attached to scenarios

### Empty Reports
- Run tests first to generate Cucumber JSON
- Check that feature files are being executed
- Verify test scenarios are completing

## 📚 Additional Resources

- [Cucumber Documentation](https://cucumber.io/docs)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [Gson Documentation](https://github.com/google/gson)

