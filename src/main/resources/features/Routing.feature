Feature: Positive scenario test for tier 3 APIs through channel service.

  Scenario Outline: A successful routing response
    Given a valid bin "<bin>", dbid "<dbid>", iam_subject "<iam_subject>"
    And a valid routing request
    When routing api is called
    Then a successful routing response is returned

    Examples:
      | bin        | dbid       | iam_subject |
      | 1130559045 | 1112540066 | 1158070858  |
      | 1145739750 | 1911490651 | 1551266769  |
