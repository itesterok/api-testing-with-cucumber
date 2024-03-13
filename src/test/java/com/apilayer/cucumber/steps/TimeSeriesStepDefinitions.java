package com.apilayer.cucumber.steps;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.apilayer.api.fixerapi.symbols.FixerCurrency;
import com.apilayer.api.fixerapi.timeseries.api.TimeseriesService;
import com.apilayer.api.fixerapi.timeseries.model.TimeseriesRequest;
import com.apilayer.api.fixerapi.timeseries.model.TimeseriesResponse;
import com.apilayer.api.fixerapi.utils.ApiKeyUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@CucumberContextConfiguration
@SpringBootTest
public class TimeSeriesStepDefinitions {

    private final Map<String, String> optionalHeaders = new HashMap<>();

    @Autowired
    private TimeseriesService timeseriesService;

    @Autowired
    private ApiKeyUtil apiKeyUtil;

    private String startDate;

    private String endDate;

    private String base;

    private String symbols;

    private ResponseEntity<TimeseriesResponse> response;

    private String usedApiKey;

    @Given("the client is authorized with valid fixer apikey")
    public void theClientIsAuthorizedWithValidFixerApikey() {
        usedApiKey = apiKeyUtil.getValidFixerApiKey();
    }

    @Given("the client is authorized with empty apikey")
    public void theClientIsAuthorizedWithEmptyApikey() {
        usedApiKey = Strings.EMPTY;
    }

    @Given("the client is authorized with non-fixer apikey")
    public void theClientIsAuthorizedWithNonFixerApikey() {
        usedApiKey = apiKeyUtil.getNonFixerApiKey();
    }

    @Given("the client is authorized with valid expired fixer apikey")
    public void theClientIsAuthorizedWithValidExpiredFixerApikey() {
        usedApiKey = apiKeyUtil.getFixerApiKeyWithExhaustedRateLimits();
    }

    @And("the client sets header {string} to value {string}")
    public void theClientSetsHeaderHostToValueHttpsApiApilayerCom(String header, String value) {
        this.optionalHeaders.put(header, value);
    }

    @Given("the client sets arguments as following:")
    public void theClientSetsArgumentsAsFollowing(DataTable table) {
        List<String> values = table.row(1);
        startDate = values.get(0);
        endDate = values.get(1);
        base = Optional.ofNullable(values.get(2)).orElseGet(String::new).trim();
        symbols = Optional.ofNullable(values.get(3)).orElseGet(String::new).trim();
    }

    @When("^the client calls '(.*)' endpoint")
    public void theClientCallFixerTimeseriesEndpoint(String uri) {
        response =
            timeseriesService
                .withApiKey(usedApiKey)
                .withUri(uri)
                .withHeaders(optionalHeaders)
                .getTimeseries(
                    TimeseriesRequest.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .base(base)
                        .symbols(symbols)
                        .build());
    }

    @Then("^the client receives status code of (\\d+)$")
    public void the_client_receives_status_code_of(int statusCode) {
        assertThat(response.getStatusCode().value()).isEqualTo(statusCode);
    }

    @And("the client receives body with following content:")
    public void theClientReceivesBodyAsFollowing(DataTable table) {
        List<String> values = table.row(1);

        List<LocalDate> dateRange = parseStartAndEndDates(values.get(0));
        LocalDate expectedStartDate = dateRange.get(0);
        LocalDate expectedEndDate = dateRange.get(1);

        FixerCurrency expectedBase = FixerCurrency.valueOf(values.get(1));
        List<FixerCurrency> expectedSymbols = FixerCurrency.parseAsCurrencies(values.get(2));

        TimeseriesResponse body = response.getBody();
        assertThat(body).isNotNull();

        assertThat(body.getSuccess()).isTrue();
        assertThat(body.getTimeseries()).isTrue();
        assertThat(body.getStartDate()).isEqualTo(expectedStartDate);
        assertThat(body.getEndDate()).isEqualTo(expectedEndDate);
        assertThat(body.getBase()).isEqualTo(expectedBase);
        assertThat(body.getRates()).isNotNull();

        expectedStartDate
            .datesUntil(expectedEndDate)
            .forEach(
                date -> {
                    Map<FixerCurrency, Double> currentDate = body.getRates().get(date);
                    assertThat(currentDate).isNotNull();

                    expectedSymbols.forEach(
                        fixerCurrency -> {
                            assertThat(currentDate.get(fixerCurrency)).isGreaterThan(0);
                        });
                    assertThat(currentDate.keySet().size()).isEqualTo(expectedSymbols.size());
                });
    }

    @And("the client receives body with following exchange rates:")
    public void theClientReceivesBodyWithFollowingExchangeRates(DataTable table) {
        List<String> values = table.row(1);

        List<LocalDate> dateRange = parseStartAndEndDates(values.get(0));
        LocalDate expectedStartDate = dateRange.get(0);
        LocalDate expectedEndDate = dateRange.get(1);

        FixerCurrency symbol = FixerCurrency.valueOf(values.get(1));

        Double rate = Double.parseDouble(values.get(2));

        TimeseriesResponse body = response.getBody();
        assertThat(body).isNotNull();

        expectedStartDate
            .datesUntil(expectedEndDate)
            .forEach(
                date -> {
                    Map<FixerCurrency, Double> currentDate = body.getRates().get(date);
                    assertThat(currentDate).isNotNull();
                    assertThat(currentDate.get(symbol)).isEqualTo(rate);
                });
    }

    @And("the client receives error response with message '(.*)'$")
    public void the_client_receives_error_response_with_message_no_api_key_found_in_request(
        String message) {
        assertThat(Objects.requireNonNull(response.getBody()).getErrorMessage()).isEqualTo(message);
    }

    @And("the client receives custom server error response with following details:")
    public void theClientReceivesCustomServerErrorResponseWithFollowingDetails(DataTable table) {
        List<String> values = table.row(1);
        Integer code = Integer.parseInt(values.get(0));
        String type = values.get(1);
        String info = values.get(2);

        assertAll(
            "Checking response details",
            () -> assertThat(response.getBody().getError().getCode()).isEqualTo(code),
            () -> assertThat(response.getBody().getError().getType()).isEqualTo(type),
            () -> assertThat(response.getBody().getError().getInfo()).isEqualTo(info));
    }

    private List<LocalDate> parseStartAndEndDates(String value) {
        String[] dateRange = value.split(",");

        LocalDate startDate;
        LocalDate endDate;

        if (dateRange.length == 1) {
            startDate = endDate = LocalDate.parse(dateRange[0]);
        }
        else if (dateRange.length == 2) {
            startDate = LocalDate.parse(dateRange[0].trim());
            endDate = LocalDate.parse(dateRange[1].trim());
        }
        else {
            throw new IllegalArgumentException("Wrong date range is specified!");
        }

        return List.of(startDate, endDate);
    }
}
