package com.apilayer.api.config;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

@Component
public class NoOpRestTemplateResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        // do nothing, it's OK if we receive the error - we might use this for negative testing
    }
}
