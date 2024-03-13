Feature: rates can be fetched with valid data

  Scenario: fetching rates for specified currency and symbols
    Given the client is authorized with valid fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols  |
      | 2024-03-10 | 2024-03-11 | USD  | EUR, PLN |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 200
    And the client receives body with following content:
      | dates-range            | base | symbols  |
      | 2024-03-10, 2024-03-11 | USD  | EUR, PLN |

  Scenario: fetching rates for specified currency but empty (default) symbols
    Given the client is authorized with valid fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols |
      | 2024-03-10 | 2024-03-11 | USD  |         |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 200
    And the client receives body with following content:
      | dates-range            | base | symbols          |
      | 2024-03-10, 2024-03-11 | USD  | _ALL_CURRENCIES_ |

  Scenario: fetching rates for empty (default) currency and empty (default) symbols
    Given the client is authorized with valid fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols |
      | 2024-03-11 | 2024-03-11 |      |         |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 200
    And the client receives body with following content:
      | dates-range | base | symbols          |
      | 2024-03-11  | EUR  | _ALL_CURRENCIES_ |

  Scenario: fetching rates for specified currency and specified the same symbol
    Given the client is authorized with valid fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols |
      | 2024-03-11 | 2024-03-13 | PLN  | PLN     |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 200
    And the client receives body with following content:
      | dates-range            | base | symbols |
      | 2024-03-11, 2024-03-13 | PLN  | PLN     |
    And the client receives body with following exchange rates:
      | dates-range            | symbol | rate |
      | 2024-03-11, 2024-03-13 | PLN    | 1    |