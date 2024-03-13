package com.apilayer.api.common;

import static com.apilayer.api.common.HeaderUtility.addHeadersToRequest;

import java.util.Map;
import lombok.Builder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Builder(builderMethodName = "build")
public final class HttpClient {

    public static final String APPLICATION_JSON = MediaType.APPLICATION_JSON_VALUE;

    private String baseUrl;

    private String uri;

    private Map<String, Object> uriVariables;

    private MultiValueMap<String, String> queryParams;

    private HttpHeaders headers;

    private HttpHeaders authorizationHeaders;

    @Builder.Default
    private boolean addDefaultHeaders = true;

    private Object body;

    private HttpMethod method;

    private RestTemplate restTemplate;

    public <T> ResponseEntity<T> performRequest(ParameterizedTypeReference<T> returnType) {

        String path =
            uriVariables == null
                ? uri
                : UriComponentsBuilder.fromPath(uri).buildAndExpand(uriVariables).toUriString();

        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).path(path);
        final UriComponents uriComponents =
            queryParams != null ? builder.queryParams(queryParams).build() : builder.build();
        final RequestEntity.BodyBuilder requestBuilder =
            RequestEntity.method(method, uriComponents.toUri());

        if (addDefaultHeaders) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
            headers.add(HttpHeaders.ACCEPT, APPLICATION_JSON);
            addHeadersToRequest(headers, requestBuilder);
        }
        if (authorizationHeaders != null) {
            addHeadersToRequest(authorizationHeaders, requestBuilder);
        }
        if (headers != null) {
            addHeadersToRequest(headers, requestBuilder);
        }

        RequestEntity<Object> requestEntity = requestBuilder.body(body);

        return restTemplate.exchange(requestEntity, returnType);
    }
}
