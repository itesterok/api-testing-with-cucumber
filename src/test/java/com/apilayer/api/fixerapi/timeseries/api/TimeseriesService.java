package com.apilayer.api.fixerapi.timeseries.api;

import java.util.HashMap;
import java.util.Map;

import com.apilayer.api.common.HttpClient;
import com.apilayer.api.fixerapi.timeseries.model.TimeseriesRequest;
import com.apilayer.api.fixerapi.timeseries.model.TimeseriesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class TimeseriesService {

    private static String TIMESERIES_URL = "/fixer/timeseries";

    private final ThreadLocal<Map<String, String>> optionalHeaders = ThreadLocal.withInitial(HashMap::new);

    private final ThreadLocal<String> apiKey = ThreadLocal.withInitial(String::new);

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    public TimeseriesService withApiKey(String apiKey) {
        this.apiKey.set(apiKey);
        return this;
    }

    public TimeseriesService withUri(String uri) {
        TIMESERIES_URL = uri;
        return this;
    }

    public TimeseriesService withHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            this.optionalHeaders.set(new HashMap<>());
        }
        else {
            this.optionalHeaders.get().putAll(headers);
        }
        return this;
    }

    @Retryable(backoff = @Backoff(delay = 10000, maxDelay = 60000))
    public ResponseEntity<TimeseriesResponse> getTimeseries(TimeseriesRequest timeseriesRequest) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("apikey", apiKey.get());
        optionalHeaders.get().forEach(headers::add);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("start_date", timeseriesRequest.getStartDate());
        queryParams.add("end_date", timeseriesRequest.getEndDate());
        queryParams.add("base", timeseriesRequest.getBase());
        queryParams.add("symbols", timeseriesRequest.getSymbols());

        return HttpClient.build()
            .baseUrl(baseUrl)
            .uri(TIMESERIES_URL)
            .headers(headers)
            .addDefaultHeaders(false)
            .method(HttpMethod.GET)
            .queryParams(queryParams)
            .restTemplate(restTemplate)
            .build()
            .performRequest(new ParameterizedTypeReference<TimeseriesResponse>() {
            });
    }
}
