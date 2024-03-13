package com.apilayer.api.config;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import static io.qameta.allure.Allure.step;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public final class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

    public ClientHttpResponse intercept(
        HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        List<String> requestMessages = new ArrayList<>();

        requestMessages.add("\n======================== Request begin =============================");
        requestMessages.add("[URI]          : " + request.getMethod() + " " + request.getURI());
        requestMessages.add("[Headers]      : " + request.getHeaders());
        requestMessages.add("[Request body] : " + new String(body, UTF_8));
        requestMessages.add("======================== Request end ===============================");

        logMessage("Request log", requestMessages);
    }

    public void logResponse(ClientHttpResponse response) throws IOException {
        List<String> responseMessages = new ArrayList<>();

        String responseBody = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());

        if (responseBody.contains("DOCTYPE html")) {
            responseBody = "DOCTYPE html";
        }

        responseMessages.add("\n====================== Response begin =============================");
        responseMessages.add("[Status code]   : " + response.getStatusCode());
        responseMessages.add("[Headers]       : " + response.getHeaders());
        responseMessages.add("[Response body] : " + responseBody);
        responseMessages.add("====================== Response end ===============================");

        logMessage("Response log", responseMessages);
    }

    private void logMessage(String label, List<String> messages) {
        step(label, () -> messages.forEach(Allure::step));
        log.info(StringUtils.join(messages, "\n"));
    }
}
