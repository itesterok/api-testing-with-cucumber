package com.apilayer.api.common;

import java.util.List;
import java.util.Map;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;

@UtilityClass
public final class HeaderUtility {
    public static void addHeadersToRequest(
        HttpHeaders headers, RequestEntity.BodyBuilder requestBuilder) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            List<String> values = entry.getValue();
            for (String value : values) {
                if (value != null) {
                    requestBuilder.header(entry.getKey(), value);
                }
            }
        }
    }
}
