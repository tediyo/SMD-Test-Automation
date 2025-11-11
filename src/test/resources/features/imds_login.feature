Feature: IMDS Login
  As a user
  I want to login to the IMDS application
  So that I can access the system with terminal ID

  @smokee
  Scenario: Login to IMDS with terminal ID
    Given I navigate to the IMDS login page
    When I enter terminal ID "ID__1"
    And I click the IMDS login button
    Then I should be logged into IMDS successfully
    And I should see the IMDS dashboard or home page

