Feature: handling error codes

  Scenario: checking 400 response code: Bad Request
    Given the client is authorized with empty apikey
    And the client sets header 'Host' to value 'https://api.apilayer.com'
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols  |
      | 2024-03-10 | 2024-03-11 | USD  | EUR, PLN |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 400

  Scenario: checking 401 response code - Unauthorized: No valid API key provided.
    Given the client is authorized with empty apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols  |
      | 2024-03-10 | 2024-03-11 | USD  | EUR, PLN |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 401
    And the client receives error response with message 'No API key found in request'

  Scenario: checking 403 response code - Forbidden: You cannot consume this service
    Given the client is authorized with non-fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols  |
      | 2024-03-10 | 2024-03-11 | USD  | EUR, PLN |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 403
    And the client receives error response with message 'You cannot consume this service'

  Scenario: checking 404 response code - Not Found: The requested resource doesn't exist.
    Given the client is authorized with valid fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols  |
      | 2024-03-10 | 2024-03-11 | USD  | EUR, PLN |
    When the client calls '/mixer' endpoint
    Then the client receives status code of 404
    And the client receives error response with message 'no Route matched with those values'

  Scenario: checking 429 response code - Too many requests: API request limit exceeded
    Given the client is authorized with valid expired fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols  |
      | 2024-03-10 | 2024-03-11 | USD  | EUR, PLN |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 429
    And the client receives error response with message 'You have exceeded your daily/monthly API rate limit. Please review and upgrade your subscription plan at https://promptapi.com/subscriptions to continue.'

  Scenario: checking custom error code: 106 - no_rates_available
    Given the client is authorized with valid fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols |
      | 2034-03-10 | 2034-03-10 | USD  | EUR     |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 200
    And the client receives custom server error response with following details:
      | code | type               | info                                                     |
      | 106  | no_rates_available | Your query did not return any results. Please try again. |

  Scenario: checking custom error code: 201 - invalid_base_currency
    Given the client is authorized with valid fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base         | symbols |
      | 2024-03-10 | 2024-03-11 | NOT_EXISTING | EUR     |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 200
    And the client receives custom server error response with following details:
      | code | type                  | info |
      | 201  | invalid_base_currency |      |

  Scenario: checking custom error code: 202 - invalid_currency_codes
    Given the client is authorized with valid fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols      |
      | 2024-03-10 | 2024-03-11 | USD  | NOT_EXISTING |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 200
    And the client receives custom server error response with following details:
      | code | type                   | info                                                                                                |
      | 202  | invalid_currency_codes | You have provided one or more invalid Currency Codes. [Required format: currencies=EUR,USD,GBP,...] |

  Scenario: checking custom error code: 502 - invalid_start_date
    Given the client is authorized with valid fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols  |
      | yyyy-mm-dd | 2024-03-11 | USD  | EUR, PLN |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 200
    And the client receives custom server error response with following details:
      | code | type               | info                                                                                                                                           |
      | 502  | invalid_start_date | You have specified an invalid start date. Please try again or refer to the API documentation available at https://serpstack.com/documentation. |

  Scenario: checking custom error code: 503 - invalid_end_date
    Given the client is authorized with valid fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols  |
      | 2024-03-10 | yyyy-mm-dd | USD  | EUR, PLN |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 200
    And the client receives custom server error response with following details:
      | code | type             | info                                                                                                                                         |
      | 503  | invalid_end_date | You have specified an invalid end date. Please try again or refer to the API documentation available at https://serpstack.com/documentation. |

  Scenario: checking custom error code: 504 - invalid_time_frame
    Given the client is authorized with valid fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols  |
      | 2025-03-10 | 2024-03-11 | USD  | EUR, PLN |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 200
    And the client receives custom server error response with following details:
      | code | type               | info                                                                                                     |
      | 504  | invalid_time_frame | You have entered an invalid Time-Frame. [Required format: ...&start_date=YYYY-MM-DD&end_date=YYYY-MM-DD] |

  Scenario: checking custom error code: 505 - time_frame_too_long
    Given the client is authorized with valid fixer apikey
    Given the client sets arguments as following:
      | start-date | end-date   | base | symbols  |
      | 2020-03-10 | 2024-03-11 | USD  | EUR, PLN |
    When the client calls '/fixer/timeseries' endpoint
    Then the client receives status code of 200
    And the client receives custom server error response with following details:
      | code | type                | info                                                    |
      | 505  | time_frame_too_long | The Time-Frame you entered is too long. [max. 365 days] |