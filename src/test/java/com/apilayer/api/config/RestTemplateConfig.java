package com.apilayer.api.config;

import java.time.Duration;
import java.util.Collections;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .requestFactory(
                (settings) -> new BufferingClientHttpRequestFactory(configureRequestFactory()))
            .messageConverters(
                new ByteArrayHttpMessageConverter(),
                new StringHttpMessageConverter(),
                new StringHttpMessageConverter(),
                new ResourceHttpMessageConverter(),
                new ResourceRegionHttpMessageConverter(),
                new SourceHttpMessageConverter<>(),
                new AllEncompassingFormHttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(),
                new Jaxb2RootElementHttpMessageConverter())
            .additionalInterceptors(new RequestResponseLoggingInterceptor())
            .errorHandler(new NoOpRestTemplateResponseErrorHandler())
            .additionalMessageConverters(getConverter())
            .build();
    }

    private HttpComponentsClientHttpRequestFactory configureRequestFactory() {
        RequestConfig requestConfig =
            RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(Duration.ofSeconds(45)))
                .setResponseTimeout(Timeout.of(Duration.ofSeconds(45)))
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(5);
        connectionManager.setDefaultMaxPerRoute(5);
        CloseableHttpClient httpClient =
            HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    private CustomResponseCode400Converter getConverter() {
        CustomResponseCode400Converter converter = new CustomResponseCode400Converter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_HTML));
        return converter;
    }
}
