Feature: the edd can be retrieved and uploaded
  Scenario Outline: client makes call to POST fetchEdd
    When cucumber calls to build test data with caseNo "<index>" and "<testOutcome>" from test file
    Given the client calls fetchEdd
    Then the user will upload new edd

    Examples:
      | index   | testOutcome |
      | 1       | passed      |