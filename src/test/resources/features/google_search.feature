Feature: Google Search
  As a user
  I want to search on Google
  So that I can find information on the internet

  @smoke
  Scenario: Search for a term on Google
    Given I am on the Google homepage
    When I search for "Selenium WebDriver"
    Then I should see search results containing "Selenium"
    And the page title should contain "Selenium WebDriver"

  @regression
  Scenario: Search for another term on Google
    Given I am on the Google homepage
    When I search for "Cucumber BDD"
    Then I should see search results containing "Cucumber"
    And the page title should contain "Cucumber"
