package com.apilayer.api.fixerapi.utils;

import java.util.List;

import com.apilayer.api.common.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ApiKeyUtil {

    private static final String TIMESERIES_URL = "/fixer/timeseries";

    private static final Integer minRateLimit = 1;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.fixer-api-key}")
    private String fixerApiKeys;

    @Value("${app.fixer-api-key-expired}")
    private String expiredFixerApiKey;

    @Value("${app.number-verification-api-key}")
    private String numberVerificationApiKey;

    @Autowired
    private RestTemplate restTemplate;

    public String getFixerApiKeyWithExhaustedRateLimits() {
        return expiredFixerApiKey;
    }

    public String getNonFixerApiKey() {
        return numberVerificationApiKey;
    }

    public String getValidFixerApiKey() {
        for (String fixerApiKey : List.of(fixerApiKeys.split(","))) {
            if (getRateLimit(fixerApiKey) > minRateLimit) {
                return fixerApiKey;
            }
        }

        throw new IllegalStateException("No any valid fixer api keys found!");
    }

    private Integer getRateLimit(String apiKey) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("apikey", apiKey);

        ResponseEntity<String> response =
            HttpClient.build()
                .baseUrl(baseUrl)
                .uri(TIMESERIES_URL)
                .headers(headers)
                .addDefaultHeaders(false)
                .method(HttpMethod.GET)
                .restTemplate(restTemplate)
                .build()
                .performRequest(new ParameterizedTypeReference<>() {
                });

        List<String> remainingMonthLimits = response.getHeaders().get("X-RateLimit-Remaining-Month");
        List<String> remainingDayLimits = response.getHeaders().get("X-RateLimit-Remaining-Month");

        // there is a bug in https://apilayer.com/marketplace/fixer that sometimes it does not return
        // headers with rate limit information. In such case, to overcome the issue and assure we can
        // use the method safely, we return 0 indicating this apikey exhausted its rates limits.
        if (remainingDayLimits == null || remainingMonthLimits == null) {
            return 0;
        }

        return Math.min(
            Integer.parseInt(remainingMonthLimits.get(0)), Integer.parseInt(remainingDayLimits.get(0)));
    }
}
